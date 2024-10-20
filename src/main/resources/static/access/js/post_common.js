// post_common.js
// 全域變數，用於新增貼文時的圖片和影片列表
let imageFileList = [];
let videoFileList = [];

function handleFiles(files, previewContainer, imageFileList, videoFileList, existingFilesCount = 0) {

    // 檢查檔案大小、數量
    const maxFileSize = 2 * 1024 * 1024; // 2MB
    const maxFiles = 3;


    // 在計算 totalFiles 時，包含 existingFilesCount，考慮現有的圖片和影片
    const totalFiles = existingFilesCount + imageFileList.length + videoFileList.length + files.length;
    if (totalFiles > maxFiles) {
        alert('一則貼文最多上傳3個檔案');
        return;
    }
    for (let i = 0; i < files.length; i++) {
        if (files[i].size > maxFileSize) {
            alert('檔案大小不得超過2MB');
            return;
        }
    }

    // 檔案預覽
    Array.from(files).forEach((file) => {
        const previewItem = document.createElement('div');
        previewItem.classList.add('preview-item');

        let element;
        if (file.type.startsWith('image/')) {
            element = document.createElement('img');
            element.src = URL.createObjectURL(file);
            imageFileList.push(file);  // 把圖片檔案推到 imageFileList

        } else if (file.type.startsWith('video/')) {
            element = document.createElement('video');
            element.src = URL.createObjectURL(file);
            element.controls = true;
            videoFileList.push(file);  // 把影片檔案推到 videoFileList
        }

        // 生成右上角的 "叉叉" 刪除按鈕
        const removeBtn = document.createElement('button');
        removeBtn.classList.add('remove-btn');
        removeBtn.innerHTML = '&times;';  // 顯示叉叉符號

        // 使用檔案物件本身來從列表中移除，確保正確的檔案被移除
        removeBtn.addEventListener('click', () => {
            previewItem.remove();  // 移除該項目的預覽

            if (file.type.startsWith('image/')) {
                const index = imageFileList.indexOf(file);
                if (index > -1) {
                    imageFileList.splice(index, 1);
                }

            } else if (file.type.startsWith('video/')) {
                const index = videoFileList.indexOf(file);
                if (index > -1) {
                    videoFileList.splice(index, 1);
                }

            }


            resetFileInput('media');  // 重置圖、影片上傳區域


        });

        previewItem.appendChild(element);
        previewItem.appendChild(removeBtn);
        previewContainer.appendChild(previewItem);

    });
}

// 重置 input file 元素，允許再次上傳
function resetFileInput(id) {
    const input = document.getElementById(id);
    if (input) {
        input.value = '';  // 重置 input 的值，允許重新選擇相同檔案
    }
}

// 其他函式保持不變，如 removePost, displayPost 等
function removePost(post) {
    const postDiv = document.querySelector(`[data-post-id='${post.postId}']`);

    if (postDiv) {
        postDiv.remove();
    }
}

function displayPost(post, prepend = true) {
    console.log('post', post);
    const postsDiv = document.getElementById('posts');

    // 檢查貼文是否已存在
    let existingPostDiv = postsDiv.querySelector(`[data-post-id='${post.postId}']`);
    if (existingPostDiv) {
        return; // 貼文已存在，避免重複
    }

    const postDiv = document.createElement('div');
    postDiv.className = 'post';
    // 加入 data-post-id 屬性，方便後續更新
    postDiv.setAttribute('data-post-id', post.postId);
    // 建立使用者名稱和發佈時間
    const {headerDiv} = createPostHeader(post);
    postDiv.appendChild(headerDiv);


    // 建立貼文內容
    const content = createPostContent(post);
    postDiv.appendChild(content);
    // 以容器管理 media-container
    const mediaContainer = document.createElement('div');
    mediaContainer.classList.add('media-container');
    postDiv.appendChild(mediaContainer);

    // 加入圖片或影片
    if (post.images && post.images.length > 0) addMediaElements(mediaContainer, post.images, 'img');
    if (post.videos && post.videos.length > 0) addMediaElements(mediaContainer, post.videos, 'video');

    // 建立按讚數和留言數顯示區域
    const {postStats, thumbsCount, commentsCount} = createPostStats(post);
    postDiv.appendChild(postStats);

    // 建立按讚按鈕和留言輸入框
    const postActions = createPostActions(post, thumbsCount, commentsCount);
    postDiv.appendChild(postActions);
    // 新增留言區域
    const postComments = document.createElement('div')
    postComments.className = "post-comments"
    // 填充留言區域
    renderComments(post.comments, postComments, replyNum, post.postId);
    // 顯示更多留言字樣
    loadMoreReply(post.replyCount, replyNum, postComments, post.postId);
    postDiv.appendChild(postComments);
    // 點擊讚數顯示按讚用戶
    thumbsCount.addEventListener('click', () => {
        showThumbUsers(post.postId, thumbsCount);
    });
    // 點擊留言數顯示貼文詳細頁面
    commentsCount.addEventListener('click', () => {
        showPostDetails(post.postId);
    });
    // 將貼文插入 DOM
    if (prepend) {
        postsDiv.prepend(postDiv); // 將貼文顯示在 posts 頂部
    } else {
        postsDiv.appendChild(postDiv);
    }
}

/* 讓使用者調整貼文用的視窗 */
function showEditPostForm(post) {
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${post.postId}?userId=${currentUserId}`)
            .then(response => response.json())
            .then(post => {
                // 定義本地的圖片和影片列表，避免與全域變數混淆
                let imageFileList = [];
                let videoFileList = [];
                let existingImages = [...post.images]; // 複製現有的圖片列表
                let existingVideos = [...post.videos]; // 複製現有的影片列表


                // 檢查是否已經有顯示的編輯表單
                let editFormModal = document.querySelector('.edit-post-modal');

                if (!editFormModal) {
                    editFormModal = document.createElement('div');
                    editFormModal.className = 'edit-post-modal';
                    editFormModal.innerHTML = `
            <div class="modal-content">
                <button class="close-button">X</button>
                <form id="editPostForm" enctype="multipart/form-data">
                    <textarea id="editPostContent">${post.content}</textarea>
                    <div class="file-upload">
                        <label for="editMedia">修改相片 / 影片</label>
                        <input type="file" id="editMedia" accept="image/*, video/*" multiple>
                    </div>
                    <div class="preview-container" id="editPreviewContainer"></div>
                    <button id="updatePostButton" type="button">更新貼文</button>
                </form>
            </div>
        `;
                    document.body.appendChild(editFormModal);
                }
                // 更新文字區域的內容
                const editPostContent = editFormModal.querySelector('#editPostContent');
                editPostContent.value = post.content;

                // 清空並重新載入預覽區域
                const previewContainer = editFormModal.querySelector('#editPreviewContainer');

                previewContainer.innerHTML = '';

                // 既有圖片和影片：使用 S3 URL 提供預覽
                existingImages.forEach((imageUrl) => {
                    const previewItem = document.createElement('div');
                    previewItem.classList.add('preview-item');
                    const imgElement = document.createElement('img');
                    imgElement.src = imageUrl;

                    const removeBtn = document.createElement('button');
                    removeBtn.classList.add('remove-btn');
                    removeBtn.innerHTML = '&times;';  // 顯示叉叉符號

                    // 使用 imageUrl 來確保正確移除圖片
                    removeBtn.addEventListener('click', () => {
                        const index = existingImages.indexOf(imageUrl);
                        if (index > -1) {
                            existingImages.splice(index, 1);  // 移除該圖片
                        }
                        previewItem.remove();  // 從畫面中移除預覽
                    });

                    previewItem.appendChild(imgElement);
                    previewItem.appendChild(removeBtn);
                    previewContainer.appendChild(previewItem);
                });

                existingVideos.forEach((videoUrl) => {
                    const previewItem = document.createElement('div');
                    previewItem.classList.add('preview-item');
                    const videoElement = document.createElement('video');
                    videoElement.src = videoUrl;
                    videoElement.controls = true;

                    const removeBtn = document.createElement('button');
                    removeBtn.classList.add('remove-btn');
                    removeBtn.innerHTML = '&times;';  // 顯示叉叉符號

                    // 使用 videoUrl 來確保正確移除影片
                    removeBtn.addEventListener('click', () => {
                        const index = existingVideos.indexOf(videoUrl);
                        if (index > -1) {
                            existingVideos.splice(index, 1);  // 移除該影片
                        }
                        previewItem.remove();  // 從畫面中移除預覽
                    });

                    previewItem.appendChild(videoElement);
                    previewItem.appendChild(removeBtn);
                    previewContainer.appendChild(previewItem);
                });

                // 在新增事件監聽器之前，先移除已存在的監聽器
                // 更新按鈕
                const updatePostButton = document.getElementById('updatePostButton');
                const newUpdatePostButton = updatePostButton.cloneNode(true);
                updatePostButton.parentNode.replaceChild(newUpdatePostButton, updatePostButton);

                newUpdatePostButton.addEventListener('click', () => {
                    const content = editPostContent.value;

                    // 檢查字數是否超過 1000
                    if (content.length > 1000) {
                        alert("文字長度不可超過1000字");
                        return; // 阻止更新貼文
                    }
                    updatePost(post.postId, imageFileList, videoFileList, existingImages, existingVideos);

                });

                // 圖片/影片輸入框
                const editMediaInput = document.getElementById('editMedia');
                const newEditMediaInput = editMediaInput.cloneNode(true);
                editMediaInput.parentNode.replaceChild(newEditMediaInput, editMediaInput);

                newEditMediaInput.addEventListener('change', function (event) {

                    const existingFilesCount = existingImages.length + existingVideos.length;
                    handleFiles(event.target.files, previewContainer, imageFileList, videoFileList, existingFilesCount);  // 使用 handleFiles 處理新上傳的檔案
                });

                // 在畫面上顯示調整貼文視窗
                editFormModal.style.display = 'block';

                // 關閉按鈕
                const closeButton = editFormModal.querySelector('.close-button');
                const newCloseButton = closeButton.cloneNode(true);
                closeButton.parentNode.replaceChild(newCloseButton, closeButton);

                // 點擊叉叉時，重置本地的圖片和影片列表，避免影響後續操作
                newCloseButton.addEventListener('click', () => {

                    editFormModal.style.display = 'none';
                    imageFileList = [];
                    videoFileList = [];
                    existingImages = [];
                    existingVideos = [];
                });
            }).catch(error => {
            if (error.status === 401) {
                redirectToLogin();
            } else {
                console.error('Error fetching post details:', error);
            }
        })

    } else {
        // 没有 JWT token，重新登入
        redirectToLogin();
    }
}

/* 送出更新 */
function updatePost(postId, imageFileList, videoFileList, existingImages, existingVideos) {
    const content = document.getElementById('editPostContent').value;
    const formData = new FormData();

    formData.append('content', content);

    // 新上傳的圖片和影片
    imageFileList.forEach(file => {
        formData.append('newImages', file);
    });
    videoFileList.forEach(file => {
        formData.append('newVideos', file);
    });

    // 保留現有的圖片和影片
    existingImages.forEach(imageUrl => formData.append('existingImages', imageUrl));
    existingVideos.forEach(videoUrl => formData.append('existingVideos', videoUrl));


    // 發送 PUT 請求
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${postId}`, {method: 'PUT', body: formData})
            .then(response => response.json())
            .then(updatedPost => {

                stompClient.send(`/app/notify/post/update`, {}, JSON.stringify(updatedPost));
                // 關閉編輯表單
                document.querySelector('.edit-post-modal').style.display = 'none';
            })
            .catch(error => {
                if (error.status === 401) {
                    // JWT token 可能無效或過期，重導到登入頁面
                    redirectToLogin();
                } else {
                    console.error('Error updating post:', error);
                }
            })
    } else {
        // 沒有 JWT token，重導到登入頁面
        redirectToLogin();
    }
}

// 其他函式保持不變
function showPostDetails(postId) {
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${postId}?userId=${currentUserId}`)
            .then(response => response.json())
            .then(post => {
                // 檢查是否已經有顯示貼文詳細資訊的視窗
                let modal = document.querySelector('.post-modal');
                if (!modal) {
                    // 動態創建模態框
                    modal = document.createElement('div');
                    modal.className = 'post-modal';
                    modal.innerHTML = `
                    <div class="modal-content" data-post-id="${post.postId}">
                        <button class="close-button">X</button>
                        <div class="post-header"></div>
                        <div class="post-body"></div>
                        <div class="post-comments"></div>
                    </div>
                `;

                    // 關閉按鈕邏輯
                    modal.querySelector('.close-button').addEventListener('click', () => {
                        modal.style.display = 'none';
                    });

                    document.body.appendChild(modal);
                }

                // 使用前面的函數生成貼文標題、內容、按讚數和留言數
                const postHeader = modal.querySelector('.post-header');
                postHeader.innerHTML = '';
                const {headerDiv} = createPostHeader(post);
                postHeader.appendChild(headerDiv);

                const postBody = modal.querySelector('.post-body');
                postBody.innerHTML = '';
                const content = createPostContent(post);
                postBody.appendChild(content);

                // 加入圖片或影片
                if (post.images) addMediaElements(postBody, post.images, 'img');
                if (post.videos) addMediaElements(postBody, post.videos, 'video');

                const {postStats, thumbsCount, commentsCount} = createPostStats(post);
                postBody.appendChild(postStats);

                // 加入按讚按鈕和留言輸入框
                const postActions = createPostActions(post, thumbsCount, commentsCount, isDetailPage = true);
                postBody.appendChild(postActions);

                // 點擊讚數顯示按讚用戶
                thumbsCount.addEventListener('click', () => {
                    showThumbUsers(postId, thumbsCount);
                });
                // 填充留言區域
                const postComments = modal.querySelector('.post-comments');
                renderComments(post.comments, postComments, null, post.postId);

                modal.style.display = 'block';
            })
            .catch(error => {
                if (error.status === 401) {
                    // JWT token 可能無效或過期，重導到登入頁面
                    redirectToLogin();
                } else {
                    console.error('Error fetching post details:', error);
                }
            })
    } else {
        // 沒有 JWT token，重導到登入頁面
        redirectToLogin();
    }
}

function createPostHeader(post) {
    const headerDiv = document.createElement('div');
    headerDiv.className = 'post-header';
    const userPhotoDiv = document.createElement('div');
    userPhotoDiv.className = 'post-user-info';

    const userPhoto = document.createElement('img');
    userPhoto.src = post.userPhoto;
    userPhoto.alt = `${post.userName}'s avatar`;
    userPhoto.className = 'post-user-avatar';
    userPhoto.onclick = function () {
        window.location.href = `/profile.html?userId=${post.userId}`;
    };

    const infoContainer = document.createElement('div');
    infoContainer.className = 'post-header-info';

    const userName = document.createElement('h3');
    userName.textContent = post.userName;

    const time = document.createElement('small');
    const timeDiff = displayTimeDifference(calculateTimeDifference(post.createAt));
    time.textContent = `${timeDiff}`;

    infoContainer.appendChild(userName);
    infoContainer.appendChild(time);

    userPhotoDiv.appendChild(userPhoto);
    userPhotoDiv.appendChild(infoContainer);

    headerDiv.appendChild(userPhotoDiv);

    // 當 current user = 發文者，顯示編輯、刪除貼文選項
    if (currentUserId === post.userId) {
        const menuContainer = document.createElement('div');
        menuContainer.className = 'post-menu-container';

        const menuButton = document.createElement('button');
        menuButton.className = 'post-menu-button';
        menuButton.innerHTML = '&#8942;'; // 垂直的三个點

        const menuDropdown = document.createElement('div');
        menuDropdown.className = 'post-menu-dropdown';

        const editOption = document.createElement('div');
        editOption.textContent = '編輯貼文';
        editOption.onclick = () => showEditPostForm(post);

        const deleteOption = document.createElement('div');
        deleteOption.textContent = '刪除貼文';
        deleteOption.onclick = () => {
            if (confirm('確定要刪除貼文？')) {
                deletePost(post);
            }
        };

        menuDropdown.appendChild(editOption);
        menuDropdown.appendChild(deleteOption);

        menuContainer.appendChild(menuButton);
        menuContainer.appendChild(menuDropdown);

        headerDiv.appendChild(menuContainer);

        // 切換下拉選單
        menuButton.onclick = (e) => {
            e.stopPropagation();
            const isVisible = menuDropdown.style.display === 'block';
            menuDropdown.style.display = isVisible ? 'none' : 'block';

            // 檢查菜單是否會超出視窗右側
            if (!isVisible) {
                const rect = menuDropdown.getBoundingClientRect();
                const overflow = rect.right - window.innerWidth;
                if (overflow > 0) {
                    menuDropdown.style.right = `${overflow}px`;
                }
            }
        };

        // 關閉下拉選單
        document.addEventListener('click', () => {
            menuDropdown.style.display = 'none';
        });
    }

    return {headerDiv};
}

// 刪除貼文
function deletePost(post) {
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${post.postId}`, {method: 'DELETE'})
            .then(response => {
                if (response.ok) {
                    const postDiv = document.querySelector(`[data-post-id="${post.postId}"]`);
                    if (postDiv) postDiv.remove();
                    stompClient.send(`/app/notify/post/delete`, {}, JSON.stringify(post));
                } else {
                    alert('無法刪除貼文，請稍後再試');
                }
            })
            .catch(error => {
                if (error.status === 401) {
                    redirectToLogin();
                } else {
                    console.error('Error:', error);
                    alert('刪除失敗');
                }
            });
    } else {
        redirectToLogin();
    }
}

function createPostContent(post) {
    const content = document.createElement('p');
    content.textContent = post.content;
    return content;
}

function createPostStats(post) {
    const postStats = document.createElement('div');
    postStats.className = 'post-stats';

    const thumbsCount = document.createElement('span');
    thumbsCount.className = 'thumbs-count';
    thumbsCount.textContent = `${post.thumbsCount}個讚`;

    const commentsCount = document.createElement('span');
    commentsCount.className = 'comments-count';
    commentsCount.id = `comments-count-${post.postId}`
    commentsCount.textContent = `${post.replyCount}個留言`;

    postStats.appendChild(thumbsCount);
    postStats.appendChild(commentsCount);

    return {postStats, thumbsCount, commentsCount};
}

function createPostActions(post, thumbsCount, commentsCount, isDetailPage = false) {
    const postActions = document.createElement('div');
    postActions.className = 'post-actions';

    const likeButton = document.createElement('button');
    likeButton.className = 'like-button';
    likeButton.innerHTML = `<i class="fas fa-thumbs-up"></i> 讚`;
    likeButton.setAttribute('data-post-id', post.postId);

    // 根據 post.liked 初始化按鈕狀態
    let liked = post.liked;
    if (liked) {
        likeButton.classList.add('liked');
    }
    const commentInput = document.createElement('input');
    commentInput.type = 'text';
    commentInput.className = 'comment-input';
    commentInput.placeholder = '留言......';

    // 按讚按鈕點擊事件
    likeButton.addEventListener('click', () => {
        // 樣式管理：點讚視覺反饋
        liked = !liked;
        const likeButtons = document.querySelectorAll(`button.like-button[data-post-id="${post.postId}"]`);
        likeButtons.forEach(button => {
            if (liked) {
                button.classList.add('liked');
            } else {
                button.classList.remove('liked');
            }
        })
        likePost(post.postId, currentUserId, thumbsCount);
    });

    // 留言事件處理
    let isComposing = false;

    // 偵測選字開始
    commentInput.addEventListener('compositionstart', () => {
        isComposing = true;  // 進行選字狀態
    });

    // 偵測選字結束
    commentInput.addEventListener('compositionend', () => {
        isComposing = false; // 選字完成
    });

    // 留言事件處理
    commentInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !isComposing) {
            const commentContent = commentInput.value.trim();
            if (commentContent) {
                submitComment(post.postId, commentContent, commentsCount, commentInput, isDetailPage);
            }
        }
    });

    postActions.appendChild(likeButton);
    postActions.appendChild(commentInput);

    return postActions;
}

function addMediaElements(postDiv, mediaUrls, mediaType) {
    mediaUrls.forEach(url => {
        const mediaElement = document.createElement(mediaType);
        mediaElement.src = url;
        // 如果是影片，則添加 controls 屬性
        if (mediaType === 'video') {
            mediaElement.controls = true;
        }
        postDiv.appendChild(mediaElement);
    });
}

function showThumbUsers(postId, thumbsCount) {
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${postId}/thumb`)
            .then(response => response.json())
            .then(users => {

                thumbsCount.textContent = `${users.length}個讚`;

                // 檢查是否已經有顯示用戶的視窗
                let modal = document.querySelector('.thumb-modal');
                if (!modal) {
                    modal = document.createElement('div');
                    modal.className = 'thumb-modal';
                    modal.innerHTML = `
                        <h3>按讚的用戶</h3>
                        <div class="modal-content"></div>
                        <button class="close-button">關閉</button>
                    `;
                    document.body.appendChild(modal);

                    modal.querySelector('.close-button').addEventListener('click', () => {
                        modal.style.display = 'none';
                    });
                }

                const modalContent = modal.querySelector('.modal-content');
                modalContent.innerHTML = users.map(user => `
                    <div class="user-item">
                        <img src="${user.avatarUrl || defaultUserPhoto}" alt="${user.userName}" class="user-avatar" onclick="window.location.href='/profile.html?userId=${user.userId}'">
                        <span class="user-name">${user.userName}</span>
                    </div>
                `).join('');

                modal.style.display = 'block';
            })
            .catch(error => {
                if (error.status === 401) {
                    redirectToLogin();
                } else {
                    console.error('Error fetching thumb users:', error);
                }
            });
    } else {
        redirectToLogin();
    }
}

// 檢查 JWT token 並返回 true 或 false
function validateJwtOrRedirect() {
    if (checkJwtToken()) {
        return true;
    } else {
        redirectToLogin();
        return false;
    }
}

// 加載用戶頭像
async function loadUserPhoto(userId) {
    const avatarUrl = await loadUserAvatar(userId);
    return avatarUrl || defaultUserPhoto;
}

// 通用的 fetch 請求處理
async function sendPostRequest(url, data) {
    try {
        const response = await fetchWithJwt(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        return await response.json();
    } catch (error) {
        if (error.status === 401) {
            redirectToLogin();
        } else {
            console.error('Error:', error);
        }
        throw error;
    }
}

// 發送 WebSocket 更新
function notifyWebSocket(topic, updatedData) {
    stompClient.send(topic, {}, JSON.stringify(updatedData));
}

async function likePost(postId, currentUserId, thumbsCountElement) {
    if (!validateJwtOrRedirect()) return;

    const thumbInfo = {
        userId: currentUserId,
        userName: localStorage.getItem('currentUser'),
        avatarUrl: await loadUserPhoto(currentUserId)
    };

    const updatedPost = await sendPostRequest(`/api/posts/${postId}/thumb`, thumbInfo);
    thumbsCountElement.textContent = `${updatedPost.thumbsCount}個讚`;

    // 更新按讚按鈕的樣式
    const likeButtons = document.querySelectorAll(`button.like-button[data-post-id="${postId}"]`);
    likeButtons.forEach(button => {
        if (updatedPost.liked) {
            button.classList.add('liked');
        } else {
            button.classList.remove('liked');
        }
    });
    notifyWebSocket(`/app/notify/thumb/update`, updatedPost);
}


async function submitComment(postId, commentContent, commentsCountElement, commentInputElement, isDetailPage = false) {
    if (!validateJwtOrRedirect()) return;

    const commentInfo = {
        userId: currentUserId,
        userName: localStorage.getItem('currentUser'),
        content: commentContent,
        createAt: getCurrentUTCTime(),
        userPhoto: await loadUserPhoto(currentUserId)
    };

    const updatedPost = await sendPostRequest(`/api/posts/${postId}/comments`, commentInfo);
    commentsCountElement.textContent = `${updatedPost.replyCount}個留言`;
    commentInputElement.value = '';

    if (isDetailPage) {
        const postComments = document.querySelector('.post-comments');
        renderComments(updatedPost.comments, postComments, null, updatedPost.postId);
    }

    notifyWebSocket(`/app/notify/comment/update`, updatedPost);
}

window.displayPost = displayPost;
window.removePost = removePost;
window.handleCommentUpdate = handleCommentUpdate;
window.addMediaElements = addMediaElements;
window.showPostDetails = showPostDetails;

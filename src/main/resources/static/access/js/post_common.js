//post_common.js
let imageFileList = [];
let videoFileList = [];

function handleFiles(files, previewContainer) {
    Array.from(files).forEach((file, index) => {
        const previewItem = document.createElement('div');
        previewItem.classList.add('preview-item');

        let element;
        if (file.type.startsWith('image/')) {
            element = document.createElement('img');
            element.src = URL.createObjectURL(file);
            imageFileList.push(file);  // 把圖片檔案推到 imageFileList
            console.log('imageFileList push :', imageFileList)
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

        // 設置叉叉按鈕的點擊事件來移除對應的圖片或影片
        removeBtn.addEventListener('click', () => {
            previewItem.remove();  // 移除該項目的預覽

            // 從 fileList 中移除對應的檔案
            if (file.type.startsWith('image/')) {
                imageFileList = imageFileList.filter((_, i) => i !== index);  // 正確地移除圖片
                resetFileInput('media');  // 重置圖片或影片上傳
                console.log('imageFileList reset :', imageFileList);
            } else if (file.type.startsWith('video/')) {
                videoFileList = videoFileList.filter((_, i) => i !== index);  // 正確地移除影片
                resetFileInput('media');  // 重置圖片或影片上傳
            }
        });

        previewItem.appendChild(element);
        previewItem.appendChild(removeBtn);
        previewContainer.appendChild(previewItem);
    });
}


// 重置 input file 元素，允許再次上傳
function resetFileInput(id) {
    const input = document.getElementById(id);
    input.value = '';  // 重置 input 的值，允許重新選擇相同檔案
}

function removePost(post) {
    const postDiv = document.querySelector(`[data-post-id='${post.postId}']`);
    console.log('postDiv', postDiv);
    if (postDiv) {
        postDiv.remove();
    }
}

function displayPost(post, prepend = true) {
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

    const previewContainer = editFormModal.querySelector('#editPreviewContainer');
    previewContainer.innerHTML = '';

    // 既有圖片和影片：使用s3 URL 提供預覽
    post.images.forEach((imageUrl, index) => {
        const previewItem = document.createElement('div');
        previewItem.classList.add('preview-item');
        const imgElement = document.createElement('img');
        imgElement.src = imageUrl;

        const removeBtn = document.createElement('button');
        removeBtn.classList.add('remove-btn');
        removeBtn.innerHTML = '&times;';  // 顯示叉叉符號

        // 點擊叉叉移除圖片
        removeBtn.addEventListener('click', () => {
            post.images.splice(index, 1);  // 移除該圖片
            previewItem.remove();  // 從畫面中移除預覽
        });

        previewItem.appendChild(imgElement);
        previewItem.appendChild(removeBtn);
        previewContainer.appendChild(previewItem);
    });

    post.videos.forEach((videoUrl, index) => {
        const previewItem = document.createElement('div');
        previewItem.classList.add('preview-item');
        const videoElement = document.createElement('video');
        videoElement.src = videoUrl;
        videoElement.controls = true;

        const removeBtn = document.createElement('button');
        removeBtn.classList.add('remove-btn');
        removeBtn.innerHTML = '&times;';  // 顯示叉叉符號

        // 點擊叉叉移除影片
        removeBtn.addEventListener('click', () => {
            post.videos.splice(index, 1);  // 移除該影片
            previewItem.remove();  // 從畫面中移除預覽
        });

        previewItem.appendChild(videoElement);
        previewItem.appendChild(removeBtn);
        previewContainer.appendChild(previewItem);
    });

    // 點擊更新按鈕事件
    document.getElementById('updatePostButton').addEventListener('click', () => {
        updatePost(post.postId);
        console.log('updatePost(post.postId) : ', post.postId);
    });

    // 新上傳圖片/影片的預覽功能 ：以URL.createObjectURL本地顯示預覽
    document.getElementById('editMedia').addEventListener('change', function (event) {
        handleFiles(event.target.files, previewContainer);  // 使用 handleFiles 處理新上傳的檔案
    });

    // 在畫面上顯示調整貼文視窗
    editFormModal.style.display = 'block';
    // 點擊叉叉就跳出視窗
    editFormModal.querySelector('.close-button').addEventListener('click', () => {
        editFormModal.style.display = 'none';
    });
}

/* 送出更新 */
function updatePost(postId) {
    const content = document.getElementById('editPostContent').value;
    const formData = new FormData();

    formData.append('content', content);

    // 新上傳的圖片和影片
    const newMediaFiles = document.getElementById('editMedia').files;
    Array.from(newMediaFiles).forEach(file => {
        if (file.type.startsWith('image/')) {
            formData.append('newImages', file);
            // 是否會誤判，而放到 newImage?
            console.log('newImages : ', file)
        } else if (file.type.startsWith('video/')) {
            formData.append('newVideos', file);
        }
    });

    // 保留現有的圖片和影片，但過濾掉 blob URL
    const existingImages = Array.from(document.querySelectorAll('#editPreviewContainer img'))
        .map(img => img.src)
        .filter(src => !src.startsWith('blob:'));  // 過濾掉 blob URL

    const existingVideos = Array.from(document.querySelectorAll('#editPreviewContainer video'))
        .map(video => video.src)
        .filter(src => !src.startsWith('blob:'));  // 過濾掉 blob URL

    existingImages.forEach(imageUrl => formData.append('existingImages', imageUrl));
    existingVideos.forEach(videoUrl => formData.append('existingVideos', videoUrl));

    console.log('formData : {}', formData);

    // 發送 PUT 請求
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts/${postId}`, {method: 'PUT', body: formData})
            .then(response => response.json())
            .then(updatedPost => {
                console.log('updatedPost ', updatedPost);
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
                        modal.style.display = 'none'; // 隱藏模態框
                    });

                    document.body.appendChild(modal);
                }

                // 使用前面的函數生成貼文標題、內容、按讚數和留言數
                const postHeader = modal.querySelector('.post-header');
                postHeader.innerHTML = ''; // 清空之前的內容
                const {headerDiv} = createPostHeader(post);
                postHeader.appendChild(headerDiv);

                const postBody = modal.querySelector('.post-body');
                postBody.innerHTML = ''; // 清空之前的內容
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

                // 顯示模態框
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
                    alert('删除失败');
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
    // commentInput.addEventListener('keydown', (e) => {
    //     if (e.key === 'Enter') {
    //         const commentContent = commentInput.value.trim();
    //         if (commentContent) {
    //             submitComment(post.postId, commentContent, commentsCount, commentInput, isDetailPage);
    //         }
    //     }
    // });
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
                console.log('users : ', users)
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

async function likePost(postId, currentUserId, thumbsCountElement) {
    if (checkJwtToken()) {
        const thumbInfo = {
            userId: currentUserId,
            userName: localStorage.getItem('currentUser'),
            avatarUrl: ''
        };
        try {
            // 以await 確保avatarUrl被載入
            const avatarUrl = await loadUserAvatar(currentUserId);
            thumbInfo.avatarUrl = avatarUrl || defaultUserPhoto;

            const response = await fetchWithJwt(`/api/posts/${postId}/thumb`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json',},
                body: JSON.stringify(thumbInfo)
            });
            const updatedPost = await response.json();
            thumbsCountElement.textContent = `${updatedPost.thumbsCount}個讚`;
            stompClient.send(`/app/notify/thumb/update`, {}, JSON.stringify(updatedPost));
        } catch (error) {
            if (error.status === 401) {
                // JWT token 可能無效或過期，重導到登入頁面
                redirectToLogin();

            } else {
                console.error('Error:', error);
            }
        }
    } else {
        // 沒有 JWT token，重導到登入頁面
        redirectToLogin();
    }
}

async function submitComment(postId, commentContent, commentsCountElement, commentInputElement, isDetailPage = false) {
    if (checkJwtToken()) {
        const commentInfo = {
            userId: currentUserId,
            userName: localStorage.getItem('currentUser'),
            content: commentContent,
            createAt: getCurrentUTCTime(),
            userPhoto: ''
        };
        try {
            // 以await 確保avatarUrl被載入
            const userPhoto = await loadUserAvatar(currentUserId);
            commentInfo.userPhoto = userPhoto || defaultUserPhoto;

            const response = await fetchWithJwt(`/api/posts/${postId}/comments`, {
                method: 'POST',
                headers: {'Content-Type': 'application/json',},
                body: JSON.stringify(commentInfo)
            });
            const updatedPost = await response.json();
            commentsCountElement.textContent = `${updatedPost.replyCount}個留言`;
            commentInputElement.value = '';
            // 如果是在詳細頁面，將新的留言動態添加到留言區
            if (isDetailPage) {
                const postComments = document.querySelector('.post-comments');
                renderComments(updatedPost.comments, postComments);
            }
            stompClient.send(`/app/notify/comment/update`, {}, JSON.stringify(updatedPost));
        } catch (error) {
            if (error.status === 401) {
                // JWT token 可能無效或過期，重導到登入頁面
                redirectToLogin();
            } else {
                console.error('Error:', error);
            }
        }
    } else {
        // 沒有 JWT token，重導到登入頁面
        redirectToLogin();
    }
}

window.displayPost = displayPost;
window.removePost = removePost;
window.handleCommentUpdate = handleCommentUpdate;
window.addMediaElements = addMediaElements;
window.showPostDetails = showPostDetails;
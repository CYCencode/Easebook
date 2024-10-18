// common.js
let replyNum = 3;
let commentFlag = false;

/* 以名字搜尋用戶 : 開聊天室、加好友 */
function fetchUsers(username, currentUserId) {
    return fetchWithJwt(`/api/users/search?username=${username}&currentUserId=${currentUserId}`)
        .then(response => response.json())
        .catch(error => console.error('Error fetching users:', error));
}

function fetchFriends(username, currnetUserId) {
    return fetchWithJwt(`/api/users/search/friends?username=${username}&currentUserId=${currentUserId}`)
        .then(response => response.json())
        .catch(error => console.error('Error fetching users:', error));
}

/* 刪除、更新評論後，以從資料庫獲得的資訊更新畫面 */
function handleCommentUpdate(updatedPost) {
    // 查找畫面上是否存在該貼文
    const existingPost = document.querySelector(`.post[data-post-id="${updatedPost.postId}"]`);
    if (existingPost) {
        // 更新留言數
        const commentsCountElement = existingPost.querySelector('.comments-count');
        if (commentsCountElement) {
            commentsCountElement.textContent = `${updatedPost.replyCount}個留言`;
        } else {
            console.error(`找不到 comments-count-${updatedPost.postId} 元素，無法更新留言數`);
        }

        // 更新留言區域
        const commentsElement = existingPost.querySelector('.post-comments');
        renderComments(updatedPost.comments, commentsElement, replyNum, updatedPost.postId);
        loadMoreReply(updatedPost.replyCount, replyNum, commentsElement, updatedPost.postId);
    }

    // 更新詳細頁面的留言區域
    const postModal = document.querySelector('.post-modal');
    if (postModal && postModal.querySelector(`[data-post-id="${updatedPost.postId}"]`)) {
        const postComments = postModal.querySelector('.post-comments');
        const commentCount = postModal.querySelector('.comments-count');
        if (commentCount) {
            commentCount.textContent = `${updatedPost.replyCount}個留言`;
        }
        renderComments(updatedPost.comments, postComments, null, updatedPost.postId);
    }
}

/* 是否需要載入更多留言 */
function loadMoreReply(replyCount, replyNum, postComments, postId) {
    let seeMoreReply = postComments.querySelector('#seeMoreReply');
    // 檢查是否已經存在查看更多留言按鈕
    if (replyCount > replyNum && !seeMoreReply) {
        // 創建查看更多留言按鈕
        seeMoreReply = document.createElement('h4');
        seeMoreReply.id = 'seeMoreReply';
        seeMoreReply.textContent = `查看全部${replyCount}則留言`;
        // 將查看更多留言按鈕插入到留言區域的末尾
        postComments.appendChild(seeMoreReply);

        // 添加事件監聽
        seeMoreReply.addEventListener('click', () => {
            showPostDetails(postId); // 顯示完整貼文詳細內容
        });
    } else if (replyCount <= replyNum && seeMoreReply) {
        seeMoreReply.style.display = 'none';
    }
}

/* 留言區域的排序與元素生成 */

// 透過把文字放在另一個容器來避免重渲染文字時覆蓋按鈕
function renderComments(comments, container, replyNum = null, postId) {
    // 清空容器，避免重複渲染
    container.innerHTML = '';
    // 排序評論（新到舊）
    let sortedComments = comments.sort((a, b) => new Date(b.createAt) - new Date(a.createAt));
    if (replyNum) {
        sortedComments = sortedComments.slice(0, replyNum);
    }

    sortedComments.forEach(comment => {
        const commentDiv = document.createElement('div');
        commentDiv.className = 'comment';
        commentDiv.setAttribute('data-comment-id', comment.id);

        const timeDiff = displayTimeDifference(calculateTimeDifference(comment.createAt));

        // 分開評論內容和按鈕部分
        const commentContentDiv = document.createElement('div');
        commentContentDiv.className = 'comment-content';
        commentContentDiv.innerHTML = `
            <img src="${comment.userPhoto || defaultUserPhoto}" alt="${comment.userName}" class="comment-user-avatar" onclick="window.location.href='/profile.html?userId=${comment.userId}'">
            <div class="comment-text">
            <strong>${comment.userName}</strong> 
            <span>${comment.content}  </span><small>${timeDiff}</small>
            </div>
        `;
        commentContentDiv.setAttribute('data-commenter-id', comment.userId);
        commentDiv.appendChild(commentContentDiv);

        // 若是當前用戶，顯示刪除和編輯按鈕
        if (currentUserId === comment.userId) {
            // 刪除按鈕
            const deleteButton = document.createElement('button');
            deleteButton.textContent = '刪除';
            deleteButton.className = 'delete-comment-button';
            deleteButton.addEventListener('click', () => {
                // 阻擋連點刪除評論
                if (commentFlag === true) {
                    return;
                }
                commentFlag = true;
                if (checkJwtToken()) {
                    fetchWithJwt(`/api/posts/${postId}/comments/${comment.id}`, {
                        method: 'DELETE'
                    })
                        .then(response => response.json())
                        .then(updatedPost => {
                            commentFlag = false;
                            // 更新留言數顯示
                            console.log('updatedPost in post/comment fetch ', updatedPost)
                            const commentsCount = document.getElementById(`comments-count-${postId}`);
                            if (commentsCount) {
                                commentsCount.textContent = `${updatedPost.replyCount}個留言`;
                            } else {
                                console.error(`找不到 comments-count-${postId} 元素，無法更新留言數`);
                            }
                            renderComments(updatedPost.comments, container, replyNum, updatedPost.postId);
                            loadMoreReply(updatedPost.replyCount, replyNum, container, updatedPost.postId);
                            stompClient.send(`/app/notify/comment/delete`, {}, JSON.stringify(updatedPost));
                        })
                        .catch(error => {
                            if (error.status === 401) {
                                // JWT token 可能無效或過期，重導到登入頁面
                                redirectToLogin();
                            } else {
                                console.log('status :', error.status);
                                console.error('Error :', error);
                                alert('無法刪除評論，請稍後再試');
                            }
                        });
                } else {
                    // 沒有 JWT token，重導到登入頁面
                    redirectToLogin();
                }
            });
            commentDiv.appendChild(deleteButton);

            // 編輯按鈕
            const editButton = document.createElement('button');
            editButton.textContent = '編輯';
            editButton.className = 'edit-comment-button';
            editButton.addEventListener('click', () => {
                // 檢查是否已經處於編輯模式（避免重複進入編輯模式）
                if (editButton.getAttribute('data-editing') === 'true') {
                    // 取消編輯，恢復原始內容
                    commentContentDiv.innerHTML = `
                    <img src="${comment.userPhoto || defaultUserPhoto}" alt="${comment.userName}" class="comment-user-avatar" onclick="window.location.href='/profile.html?userId=${comment.userId}'">
                    <div class="comment-text">
                    <strong>${comment.userName}</strong> 
                    <span>${comment.content}  </span><small>${timeDiff}</small>
                    </div>
                `;

                    // 恢復按鈕文字和狀態
                    editButton.textContent = '編輯';
                    editButton.setAttribute('data-editing', 'false');
                    return;
                }

                // 保存原始留言內容
                const originalContent = comment.content;

                // 把評論換成輸入框
                commentContentDiv.innerHTML = ''; // 清空原內容
                const commentInput = document.createElement('input');
                commentInput.type = 'text';
                commentInput.className = 'comment-input';
                commentInput.value = originalContent;

                // 將按鈕文字切換為「取消編輯」
                editButton.textContent = '取消編輯';
                editButton.setAttribute('data-editing', 'true'); // 標記為編輯狀態

                // 將編輯輸入框添加到 DOM 中
                commentContentDiv.appendChild(commentInput);
                // 選字事件
                let isComposing = false;
                commentInput.addEventListener('compositionstart', () => {
                    isComposing = true;
                });
                commentInput.addEventListener('compositionend', () => {
                    isComposing = false;
                });
                commentInput.addEventListener('keydown', (e) => {
                    if (e.key === 'Enter' && !isComposing) {
                        const updatedContent = commentInput.value.trim();
                        if (checkJwtToken()) {
                            fetchWithJwt(`/api/posts/${postId}/comments`, {
                                method: 'PUT',
                                headers: {'Content-Type': 'application/json'},
                                body: JSON.stringify({
                                    id: comment.id,
                                    userId: comment.userId,
                                    userName: comment.userName,
                                    content: updatedContent
                                })
                            })
                                .then(response => response.json())
                                .then(updatedPost => {
                                    stompClient.send(`/app/notify/comment/update`, {}, JSON.stringify(updatedPost));
                                    commentContentDiv.innerHTML = `
                            <strong>${updatedPost.userName}</strong> <span>${updatedPost.content}</span><br>
                            <small>${timeDiff}</small>
                        `;
                                })
                                .catch(error => {
                                    if (error.status === 401) {
                                        // JWT token 可能無效或過期，重導到登入頁面
                                        redirectToLogin();
                                    } else {
                                        console.error('Error updating comment:', error);
                                        alert('無法更新評論，請稍後再試');
                                    }

                                });
                        } else {
                            // 沒有 JWT token，重導到登入頁面
                            redirectToLogin();
                        }
                    }
                });
            });
            commentDiv.appendChild(editButton);
        }

        container.appendChild(commentDiv);
    });
}

/* 時間函數 */

//  UTC+0 轉換為 UTC+8 並格式化為 YYYY-MM-DD HH:mm
function convertUTC0ToUTC8(utc0Time) {
    const createAtUTC0 = new Date(utc0Time);
    const createAtUTC8 = new Date(createAtUTC0.getTime() + 8 * 60 * 60 * 1000);
    // 使用 toISOString() 並只取得到秒的部分
    const isoString = createAtUTC8.toISOString();
    return isoString.slice(0, 19).replace('T', ' '); // 切割到 "YYYY-MM-DDTHH:mm:ss" 並將 'T' 替換為空格
}

// 取得當前的 UTC+0 時間
function getCurrentUTCTime() {
    return new Date().toISOString();
}

// 計算時間差，回傳結果為毫秒
function calculateTimeDifference(createAt) {
    const currentTime = new Date(getCurrentUTCTime()); // 當前 UTC+0 時間
    const postTime = new Date(createAt); // 貼文的 createAt 時間
    const timeDifference = currentTime - postTime; // 計算時間差（毫秒）
    return timeDifference;
}

// 將時間差轉換並顯示成幾秒前、幾分鐘前、幾小時前或幾天前發布
function displayTimeDifference(timeDifference) {
    const seconds = Math.floor(timeDifference / 1000); // 將毫秒轉換為秒
    const minutes = Math.floor(seconds / 60); // 將秒轉換為分鐘
    const hours = Math.floor(minutes / 60); // 將分鐘轉換為小時
    const days = Math.floor(hours / 24); // 將小時轉換為天數

    if (seconds < 60) {
        return `剛剛`;
    } else if (minutes < 60) {
        return `${minutes} 分鐘`;
    } else if (hours < 24) {
        return `${hours} 小時`;
    } else {
        return `${days} 天`;
    }
}

// 將 UTC+0 時間轉換為 UTC+8 並格式化為 "上午/下午 HH:mm"
function formatTimeToAMPM(utc0Time) {
    const utc8Time = convertUTC0ToUTC8(utc0Time); // 轉換成 UTC+8 的時間
    const date = new Date(utc8Time);

    let hours = date.getHours(); // 取得小時
    const minutes = date.getMinutes().toString().padStart(2, '0'); // 取得分鐘，並確保兩位數

    const ampm = hours >= 12 ? '下午' : '上午';
    hours = hours % 12; // 轉換為12小時制
    hours = hours ? hours : 12; // 小時為 0 時，顯示為 12

    return `${ampm} ${hours}:${minutes}`; // 返回 "上午/下午 HH:mm" 格式的時間
}

/* friend.js 中 displayFriendSearchResult重複的渲染邏輯 */
function getRequestButtonConfig(status) {
    switch (status) {
        case 0:
            return {buttonText: '取消好友邀請', buttonClass: 'unsendRequestButton'};
        case 1:
            return {buttonText: '確認好友邀請', buttonClass: 'confirmRequestButton'};
        case 2:
            return {buttonText: '發訊息', buttonClass: 'sendMessageButton'};
        case 3:
        default:
            return {buttonText: '加朋友', buttonClass: 'addFriendButton'};
    }
}

function handleButtonClick(buttonClass, actionCallback, replyRequest = false) {
    document.querySelectorAll(buttonClass).forEach(button => {
        button.addEventListener('click', function () {
            const userId = this.getAttribute('data-user-id');
            const receiverName = this.getAttribute('data-user-name');
            const requestId = this.getAttribute('data-request-id');
            if (replyRequest) {
                actionCallback(userId, requestId); // 用於處理好友邀請回應，如取消或確認好友
            } else {
                actionCallback(userId, receiverName); // 用於處理其他操作，如加好友或發訊息
            }
            document.getElementById('friendSearchInput').value = ''; // 重置搜尋框
            document.getElementById('friendSearchResult').style.display = 'none'; // 隱藏搜尋結果
        });
    });
}

window.fetchUsers = fetchUsers;
window.loadUserAvatar = loadUserAvatar;
window.convertUTC0ToUTC8 = convertUTC0ToUTC8;
window.getCurrentUTCTime = getCurrentUTCTime;
window.calculateTimeDifference = calculateTimeDifference;
window.displayTimeDifference = displayTimeDifference;
window.formatTimeToAMPM = formatTimeToAMPM;
window.getRequestButtonConfig = getRequestButtonConfig;

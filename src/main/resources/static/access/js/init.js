let stompClient;
const defaultUserPhoto = 'https://eve-social-media.s3.ap-northeast-1.amazonaws.com/account.png';
const defaultCoverPhoto = 'https://eve-social-media.s3.ap-northeast-1.amazonaws.com/coverphoto.jpg'
// Initialize global variables
let currentUserId = localStorage.getItem('currentUserId');
let currentUser = localStorage.getItem('currentUser');
let jwtToken = localStorage.getItem('jwtToken');
// Ensure variables are globally accessible
window.currentUserId = currentUserId;
window.currentUser = currentUser;
window.jwtToken = jwtToken;


// 載入用戶大頭照時使用
function loadUserAvatar(userId, containName = false) {
    // 檢查 JWT token 並進行資料請求
    if (checkJwtToken()) {
        return fetchWithJwt(`/api/profile/${userId}`, {method: 'GET'})
            .then(response => response.json())
            .then(profileData => {
                if (containName) {
                    return {
                        'photo': profileData.photo,
                        'username': profileData.username
                    };
                }
                return profileData.photo;
            })
            .catch(error => {
                console.error('Error:', error);
                return defaultUserPhoto;
            });
    } else {
        redirectToLogin();
        return Promise.resolve(defaultUserPhoto);
    }
}

// jwt 檢查設置
// 檢查 JWT token 是否存在於 localStorage 且有效
function checkJwtToken() {
    jwtToken = localStorage.getItem('jwtToken');
    return !!jwtToken;
}

// 從 localStorage 拿 JWT , 將 JWT token 加到 header 的屬性
function fetchWithJwt(url, options = {}) {
    jwtToken = localStorage.getItem('jwtToken')
    if (!options.headers) {
        options.headers = {};
    }
    options.headers['Authorization'] = 'Bearer ' + jwtToken;
    return fetch(url, options)
        .then(response => {
            if (response.status === 401) {
                redirectToLogin();
                throw new Error("Unauthorized");
            }
            return response;
        });
}

// redirect 到 login
function redirectToLogin() {
    alert("驗證失敗，請重新登入。");
    window.location.href = '/login.html';
}

/* 訂閱設定 */
function connect(isPost) {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    console.log('jwt token', jwtToken)
    stompClient.connect({"jwt": jwtToken}, function (frame) {
        console.log('Connected: ' + frame);
        // 按讚更新
        stompClient.subscribe(`/user/queue/notify/thumb/update`, function (thumbOutput) {
            const updatedThumb = JSON.parse(thumbOutput.body);
            console.log('updatedThumb ', updatedThumb);

            // 查找畫面上是否存在該貼文
            const existingPost = document.querySelector(`.post[data-post-id="${updatedThumb.postId}"]`);
            if (existingPost) {
                // 更新按讚數
                const thumbsCountElement = existingPost.querySelector('.thumbs-count');
                thumbsCountElement.textContent = `${updatedThumb.thumbsCount}個讚`;
            }
            // 如果有開啟的貼文詳細頁面，也同步更新按讚數
            const postModal = document.querySelector('.post-modal');
            if (postModal && postModal.querySelector(`[data-post-id="${updatedThumb.postId}"]`)) {
                const thumbsCountElement = postModal.querySelector('.thumbs-count');
                thumbsCountElement.textContent = `${updatedThumb.thumbsCount}個讚`;
            }
        });

        // 留言更新
        stompClient.subscribe(`/user/queue/notify/comment/update`, function (commentOutput) {
            const updatedPost = JSON.parse(commentOutput.body);
            handleCommentUpdate(updatedPost);
        });

        // 留言刪除
        stompClient.subscribe(`/user/queue/notify/comment/delete`, function (commentOutput) {
            const updatedPost = JSON.parse(commentOutput.body);
            handleCommentUpdate(updatedPost);
        });

        // 用戶名稱更新
        stompClient.subscribe(`/user/queue/notify/user/name/update`, function (userNameOutput) {
            const updateInfo = JSON.parse(userNameOutput.body);
            const userId = updateInfo.userId;
            const username = updateInfo.username;
            let user = document.getElementById('UserName');
            // 在主頁，判斷是否是當前用戶本人
            if (user) {
                if (userId === currentUserId) {
                    // 如果是自己，更新自己的名稱, 全局變數 currentUser
                    localStorage.setItem('currentUser', username);
                    window.currentUser = username;
                    user.textContent = username;
                    console.log("localStorage.getItem('currentUser');", localStorage.getItem('currentUser'))
                } else {
                    // 如果不是自己，更新好友圈中的名稱
                    const friendDiv = document.querySelector(`.friends[data-user-id="${userId}"]`);
                    if (friendDiv) {
                        const friendName = friendDiv.querySelector('.friend-name');
                        friendName.textContent = username;
                    }
                    // 更新聊天室姓名
                    const userName = document.querySelector(`span[data-receiver-id="${userId}"]`);
                    if (userName) {
                        userName.textContent = username;
                    }
                    // 更新聊天通知姓名
                    const chatSenderName = document.querySelector(`.message-header[id="sender-${userId}"]`);
                    if (chatSenderName) {
                        chatSenderName.querySelector('strong').textContent = username;
                    }
                }
            }


            // 不論在主頁、個人頁，好友或本人，都即時更新頁面上評論區資訊
            const commenterNames = document.querySelectorAll(`.comment-content[data-commenter-id="${userId}"]`);
            if (commenterNames) {
                commenterNames.forEach(commenterName => {
                    const commenter = commenterName.querySelector('strong');
                    commenter.textContent = username;
                });
            }
        });

        // 貼文更新
        stompClient.subscribe(`/user/queue/notify/post/update`, function (postOutput) {
            const updatedPost = JSON.parse(postOutput.body);
            console.log('updatedPost content', updatedPost);

            // 查找畫面上是否存在該貼文
            const existingPost = document.querySelector(`.post[data-post-id="${updatedPost.postId}"]`);
            console.log('document.querySelector(`.post[data-post-id="${updatedPost.postId}"]`)', document.querySelector(`.post[data-post-id="${updatedPost.postId}"]`))
            if (existingPost) {
                // 更新用戶大頭照、姓名
                const userPhotoElement = existingPost.querySelector('.post-user-avatar'); // 更新大頭照
                userPhotoElement.src = updatedPost.userPhoto;

                const userNameElement = existingPost.querySelector('.post-header-info h3'); // 更新姓名
                userNameElement.textContent = updatedPost.userName;

                // 更新貼文內容
                const contentElement = existingPost.querySelector('p'); // 更新貼文內容
                contentElement.textContent = updatedPost.content;

                // 更新圖片和影片
                const mediaContainer = existingPost.querySelector('.media-container');
                if (mediaContainer) {
                    mediaContainer.innerHTML = '';  // 清空現有的圖片和影片

                    if (updatedPost.images && updatedPost.images.length > 0) {
                        addMediaElements(mediaContainer, updatedPost.images, 'img');
                    }
                    if (updatedPost.videos && updatedPost.videos.length > 0) {
                        addMediaElements(mediaContainer, updatedPost.videos, 'video');
                    }
                }
            }

            // 如果有開啟的貼文詳細頁面，也同步更新
            const modal = document.querySelector('.post-modal');
            if (modal && modal.querySelector(`.modal-content[data-post-id="${updatedPost.postId}"]`)) {
                // 更新大頭照
                const modalUserPhotoElement = modal.querySelector('.post-user-avatar');
                modalUserPhotoElement.src = updatedPost.userPhoto;

                // 更新姓名
                const modalUserNameElement = modal.querySelector('.post-header-info h3');
                modalUserNameElement.textContent = updatedPost.userName;

                // 更新貼文內容
                const modalContentElement = modal.querySelector('.post-body p');
                modalContentElement.textContent = updatedPost.content;

                // 更新圖片和影片
                const modalMediaContainer = modal.querySelector('.post-body .media-container');
                if (modalMediaContainer) {
                    modalMediaContainer.innerHTML = ''; // 清空現有的圖片和影片
                    if (updatedPost.images && updatedPost.images.length > 0) {
                        addMediaElements(modalMediaContainer, updatedPost.images, 'img');
                    }
                    if (updatedPost.videos && updatedPost.videos.length > 0) {
                        addMediaElements(modalMediaContainer, updatedPost.videos, 'video');
                    }
                }
            }
        });
        // 訂閱好友的發文通知頻道 (接收新貼文)
        stompClient.subscribe(`/user/queue/notify/post`, function (postOutput) {
            const post = JSON.parse(postOutput.body);
            displayPost(post);
        });
        // 即時移除已刪除的貼文
        stompClient.subscribe(`/user/queue/notify/post/delete`, function (postOutput) {
            const post = JSON.parse(postOutput.body);
            console.log('delete post ', post);
            removePost(post);
        })
        // 用戶大頭照更新
        stompClient.subscribe(`/user/queue/notify/user/photo/update`, function (photoOutput) {
            const updateInfo = JSON.parse(photoOutput.body);
            console.log('/photo/update : ', updateInfo)
            const userId = updateInfo.userId;
            const photoUrl = updateInfo.photoUrl;
            // 判斷是否是當前用戶本人
            if (userId === currentUserId) {
                // 如果是自己，更新自己的大頭照
                document.getElementById('currentUserAvatar').src = photoUrl;
            } else {
                if (isPost) {
                    // 如果不是自己，更新好友圈中的大頭照
                    const friendDiv = document.querySelector(`.friends[data-user-id="${userId}"]`);
                    if (friendDiv) {
                        const friendAvatar = friendDiv.querySelector('.friend-avatar');
                        friendAvatar.src = photoUrl;
                    }
                    // 更新聊天室大頭照
                    const chatAvatar = document.querySelector(`.chat-avatar[data-receiver-id="${userId}"]`);
                    if (chatAvatar) {
                        chatAvatar.src = photoUrl;
                    }

                    // 更新聊天通知大頭照
                    const chatSender = document.querySelector(`.user-avatar[id="avatar-${userId}"]`);
                    console.log('chatSender: ', chatSender);
                    if (chatSender) {
                        chatSender.src = photoUrl;
                    }
                }
            }
            // 不論在主頁、個人頁，好友或本人，都即時更新頁面上評論區資訊
            const commenterAvatars = document.querySelectorAll(`.comment-content[data-commenter-id="${userId}"]`);
            if (commenterAvatars) {
                commenterAvatars.forEach(commenterAvatar => {
                    const commenter = commenterAvatar.querySelector('.comment-user-avatar');
                    commenter.src = photoUrl;
                });
            }
        });
        if (isPost) {
            // 訂閱接收已成立為好友, 更新好友圈、貼文
            stompClient.subscribe(`/user/queue/notify/friend/accept`, function (messageOutput) {
                const friendAcceptDTO = JSON.parse(messageOutput.body);
                handleFriendAcceptNotification(friendAcceptDTO);
            });
            // 訂閱接收者的訊息同步頻道 (聊天訊息)
            stompClient.subscribe(`/user/queue/notify/message`, function (messageOutput) {
                const message = JSON.parse(messageOutput.body);
                console.log('message chatroom, ' + message);
                // 檢查是否已經存在該 sender 的通知
                const existMessageNotification = document.getElementById('messageNotificationList').querySelector(`.message-header[id="${message.senderId}"]`);
                if (existMessageNotification) {
                    existMessageNotification.querySelector('strong').textContent = message.senderName;
                    return;
                }
                displayChatNotify(message);
            });
            // 訂閱好友邀請
            stompClient.subscribe(`/user/queue/notify/friend`, function (friendOutput) {
                const friendRequestDTO = JSON.parse(friendOutput.body);
                console.log('Received friend request:', friendRequestDTO);
                // 檢查是否已經存在該 sender 的通知
                const notificationList = document.getElementById('notificationList');
                const existingNotification = notificationList.querySelector(`[data-sender-id="${friendRequestDTO.senderId}"]`);

                if (existingNotification) {
                    // 更新 sender 名字
                    const senderSpan = existingNotification.querySelector('span');
                    senderSpan.textContent = `${friendRequestDTO.senderName} 送出了好友邀請`;
                    // 更新 senderAvatar
                    if (friendRequestDTO.senderAvatar) {
                        existingNotification.querySelector('.user-avatar').src = friendRequestDTO.senderAvatar;
                    }
                    return;
                }
                // 若不存在就調用 friend.js 中的 addNotification 函數來處理通知
                addFriendNotification(friendRequestDTO);
            });
        }
    }, function (error) {
        console.error('STOMP connection error: ' + error);
    });
}

// Initialize the application
async function initializeApp(isPost) {
    try {
        if (checkJwtToken()) {
            // Fetch user data
            const userData = await fetchWithJwt('/api/users/me', {method: 'GET'}).then(res => res.json());
            currentUser = userData.name;
            currentUserId = userData.id;
            localStorage.setItem('currentUserId', currentUserId);
            localStorage.setItem('currentUser', currentUser);

            // Update global variables
            window.currentUser = currentUser;
            window.currentUserId = currentUserId;
            if (isPost) {
                // Initialize  modules
                if (typeof initializePosts === 'function') {
                    initializePosts();
                }
                if (typeof initializeChat === 'function') {
                    initializeChat();
                }
                if (typeof initializeFriend === 'function') {
                    initializeFriend();
                }
                connect(isPost = true);
                return;
            }
            connect(isPost = false);
        } else {
            redirectToLogin();
        }
    } catch (error) {
        console.error('Error during app initialization:', error);
        redirectToLogin();
    }
}

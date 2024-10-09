//friend.js
// 搜尋好友的用戶
function searchFriendUsers(username) {
    fetchUsers(username, currentUserId).then(users => {
        displayFriendSearchResult(users);
    });
}

// 顯示搜尋好友結果
function displayFriendSearchResult(users) {
    const searchResultDiv = document.getElementById('friendSearchResult');
    searchResultDiv.innerHTML = '';  // 清空現有結果

    if (users.length > 0) {
        users.forEach(user => {
            const userDiv = document.createElement('div');
            userDiv.classList.add('user-result');
            // 依搜尋結果用戶的好友狀態，產生不同的 button 格式
            const {buttonText, buttonClass} = getRequestButtonConfig(user.status);

            // 預設 avatar 使用 defaultUserPhoto，後續異步更新
            userDiv.innerHTML = `
                <img src="${defaultUserPhoto}" alt="${user.name}'s avatar" class="user-avatar" id="avatar-${user.id}" onclick="window.location.href='/profile.html?userId=${user.id}'">
                <span>${user.name}</span>
                <button class="${buttonClass}" data-user-id="${user.id}" data-user-name="${user.name}" data-request-id="${user.friendRequestId}">${buttonText}</button>
            `;

            searchResultDiv.appendChild(userDiv);

            // 非同步載入用戶大頭照
            loadUserAvatar(user.id).then(avatarUrl => {
                const avatarImg = document.getElementById(`avatar-${user.id}`);
                avatarImg.src = avatarUrl || defaultUserPhoto;
            });
        });
        // 取消好友邀請
        handleButtonClick('.unsendRequestButton', (receiverId, requestId) => {
            replyToFriendRequest(requestId, null, false);  // 向 endpoint 發送取消好友邀請請求

            // 用 receiverId, requestId 向 WebSocket 更新交友邀請取消
            const friendRequestDTO = {
                id: requestId,
                receiverId: receiverId
            };
            stompClient.send("/app/notify/friend", {}, JSON.stringify(friendRequestDTO));
        }, true);

        // 確認好友邀請
        handleButtonClick('.confirmRequestButton', (senderId, requestId) => {
            replyToFriendRequest(requestId, senderId, true);  // 確認好友邀請
        }, true);

        // 加好友
        handleButtonClick('.addFriendButton', (receiverId, receiverName) => {
            sendFriendRequest(receiverId, receiverName);
        })

        // 發訊息
        handleButtonClick('.sendMessageButton', (receiverId, receiverName) => {
            checkChatRoom(receiverId).then(chatRoomId => {
                openChatWindow(receiverName, receiverId, chatRoomId);
            })
        });
        searchResultDiv.style.display = 'block';
    } else {
        searchResultDiv.innerHTML = '<p>無相符的使用者</p>';
        searchResultDiv.style.display = 'block';
    }
}

// 送出好友邀請
function sendFriendRequest(receiverId, receiverName) {
    const friendRequest = {
        senderId: currentUserId,
        senderName: localStorage.getItem('currentUser'),
        receiverId: receiverId,
        receiverName: receiverName,
        createAt: getCurrentUTCTime(), // 獲取當前 UTC 時間
        senderAvatar: document.getElementById('currentUserAvatar').src
    };

    fetch('/api/friend-requests', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(friendRequest)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('無法送出好友邀請，請重試');
            }
            return response.json();
        })
        .then(friendRequestDTO => {
            console.log('friend request post response.json:', friendRequestDTO);
            alert('好友邀請已送出');
            stompClient.send("/app/notify/friend", {}, JSON.stringify(friendRequestDTO));  // 通知接收者
        })
        .catch(error => console.error('Error sending friend request:', error));
}

// 新增通知
function addFriendNotification(friendRequestDTO) {
    console.log('Adding notification for:', friendRequestDTO);
    const notificationList = document.getElementById('notificationList');

    if (friendRequestDTO.senderId != null) {
        const notificationItem = document.createElement('div');
        notificationItem.setAttribute('data-sender-id', friendRequestDTO.senderId)
        notificationItem.classList.add('notification-item');
        notificationItem.innerHTML = `
        <img src="${friendRequestDTO.senderAvatar || defaultUserPhoto}" alt="${friendRequestDTO.senderName}" id="avatar-${friendRequestDTO.senderId}" class="user-avatar" onclick="window.location.href='/profile.html?userId=${friendRequestDTO.senderId}'">
        <span>${friendRequestDTO.senderName} 送出了好友邀請</span>
        <button class="acceptFriendRequest" data-request-id="${friendRequestDTO.id}">確認</button>
        <button class="rejectFriendRequest" data-request-id="${friendRequestDTO.id}">刪除</button>
        `;
        loadUserAvatar(friendRequestDTO.senderId).then(avatarUrl => {
            const avatarImg = document.getElementById(`avatar-${friendRequestDTO.senderId}`);
            avatarImg.src = avatarUrl || defaultUserPhoto;
        })
        // 將最新通知放在最上方（prepend）
        notificationList.prepend(notificationItem);

        // 更新通知數量
        updateNotificationCount(1);

        // 綁定確認和刪除按鈕事件
        notificationItem.querySelector('.acceptFriendRequest').addEventListener('click', function () {
            replyToFriendRequest(friendRequestDTO.id, friendRequestDTO.senderId, true);
        });

        notificationItem.querySelector('.rejectFriendRequest').addEventListener('click', function () {
            replyToFriendRequest(friendRequestDTO.id, null, false);
        });

        console.log('Notification list updated:', notificationList);
    } else {
        // 移除通知
        const notificationItem = notificationList.querySelector(`[data-request-id="${friendRequestDTO.id}"]`).parentNode;
        notificationList.removeChild(notificationItem);
        updateNotificationCount(-1);
    }
}

// 更新通知數量
function updateNotificationCount(change) {
    const notificationCount = document.getElementById('notificationCount');
    const currentCount = parseInt(notificationCount.textContent) || 0;
    const newCount = currentCount + change;

    if (newCount > 0) {
        notificationCount.textContent = newCount;
        notificationCount.style.display = 'inline'; // 顯示計數
    } else {
        notificationCount.textContent = '0';
        notificationCount.style.display = 'none'; // 隱藏計數
    }

    console.log('Updated notification count to:', newCount);
}

function replyToFriendRequest(id, senderId, accept) {
    fetch(`/api/friend-requests/reply?request_id=${id}&senderId=${senderId}&receiverId=${currentUserId}&accept=${accept}`, {
        method: 'POST'
    })
        .then(response => {
            if (response.ok) {
                //若接受好友邀請，解析回傳的sender 資訊並更新於好友圈
                if (accept) {
                    return response.json();
                } else {
                    return null;
                }
            } else {
                console.log('response :', response);
                throw new Error('Network response was not ok.');
            }
        }).then(profileResponseDTO => {
        alert(accept ? '已接受好友邀請' : '已刪除好友邀請');
        // 移除通知項目
        const notificationList = document.getElementById('notificationList');
        const notificationItem = notificationList.querySelector(`[data-request-id="${id}"]`).parentNode;
        notificationList.removeChild(notificationItem);
        updateNotificationCount(-1);
        if (accept && profileResponseDTO && profileResponseDTO.userId) {
            // 更新自己的主頁好友列表
            const newFriend = {
                userId: profileResponseDTO.userId,
                username: profileResponseDTO.username,
                photo: profileResponseDTO.photo || defaultUserPhoto
            };
            showFriend(newFriend);
            // 獲取新好友的第一則貼文，置於頁首
            fetchNewFriendPosts(senderId);
            // 傳送自己的資訊給 sender 更新畫面
            const acceptorInfo = {
                userId: currentUserId,
                username: localStorage.getItem('currentUser'),
                photo: document.getElementById('currentUserAvatar').src || defaultUserPhoto
            }
            stompClient.send('/app/notify/friend/accept', {}, JSON.stringify({
                senderId: senderId,
                acceptorInfo: acceptorInfo
            }));
        }
    })
        .catch(error => console.error('Error replying to friend request:', error));
}

// 處理好友接受的通知
function handleFriendAcceptNotification(acceptorInfo) {
    const newFriendId = acceptorInfo.userId;
    const newFriendName = acceptorInfo.username;
    const newFriendPhoto = acceptorInfo.photo;

    // 更新好友列表
    showFriend({
        userId: newFriendId,
        username: newFriendName,
        photo: newFriendPhoto
    });

    // 獲取新好友的第一則貼文，置於頁首
    fetchNewFriendPosts(newFriendId);
}


function initializeFriend() {
    document.getElementById('friendSearchButton').addEventListener('click', function () {
        const username = document.getElementById('friendSearchInput').value;
        if (username.trim() !== "") {
            searchFriendUsers(username);
        }
    });

    document.getElementById('friendSearchInput').addEventListener('click', function () {
        document.getElementById('friendSearchResult').style.display = 'none';
    });

    // 點擊通知圖標顯示/隱藏通知列表(toggle 切換)
    document.getElementById('notificationIcon').addEventListener('click', function () {
        const notificationList = document.getElementById('notificationList');
        notificationList.style.display = notificationList.style.display === 'none' ? 'block' : 'none';
    });

    // 載入好友通知
    fetch(`/api/friend-requests?userId=${currentUserId}`)
        .then(response => response.json())
        .then(friendRequests => {
            friendRequests.forEach(friendRequest => addFriendNotification(friendRequest));
        })
        .catch(error => console.error('Error:', error));
}

window.initializeFriend = initializeFriend;
window.addFriendNotification = addFriendNotification;
window.updateNotificationCount = updateNotificationCount;
window.replyToFriendRequest = replyToFriendRequest;
window.handleFriendAcceptNotification = handleFriendAcceptNotification;
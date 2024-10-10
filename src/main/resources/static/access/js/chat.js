const subscribedChatRooms = new Set(); // 用於追蹤已訂閱的聊天室
const newMessages = {};

function displayChatNotify(notification) {
    const senderId = notification.senderId;
    newMessages[senderId] = notification;
    console.log('message notify time, ', notification.createAt);
    // 更新訊息通知數量
    updateMessageNotificationCount();
    displayNewMessages();
}

function displayNewMessages() {
    const messageNotificationList = document.getElementById('messageNotificationList');
    messageNotificationList.innerHTML = ''; // 清空現有的內容

    // 先將 newMessages 轉換為一個可以排序的數組
    const sortedMessages = Object.values(newMessages).sort((a, b) => {
        return new Date(b.createAt) - new Date(a.createAt);  // 按照 createAt 由大到小排序
    });

    sortedMessages.forEach(notification => {
        const messageItem = document.createElement('div');
        messageItem.classList.add('message-item');
        const senderName = notification.senderName;
        const content = notification.content;
        const timeDiff = displayTimeDifference(calculateTimeDifference(notification.createAt));
        const truncatedContent = content.length > 10 ? content.substring(0, 10) + '...' : content;

        messageItem.innerHTML = `
        <img src="${defaultUserPhoto}" alt="${senderName}" id="avatar-${notification.senderId}" class="user-avatar">
        <div class="message-content">
            <div class="message-header" id="${notification.senderId}">
                <strong>${senderName}</strong>
                <span>${truncatedContent}</span>
            </div>
            <small>${timeDiff}</small>
        </div>
        `;
        loadUserAvatar(notification.senderId).then(avatarUrl => {
            const avatarImg = document.getElementById(`avatar-${notification.senderId}`);
            avatarImg.src = avatarUrl || defaultUserPhoto;
        })

        messageItem.addEventListener('click', function () {
            openChatWindow(senderName, notification.senderId, notification.chatRoomId);
            // 更新訊息已讀狀態
            fetch(`/api/chat/chat-request/`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    chatRoomId: notification.chatRoomId,
                    receiverId: notification.receiverId
                })
            })
                .then(response => {
                    if (response.ok) {
                        console.log('訊息已標記為已讀');
                    } else {
                        console.error('無法更新訊息狀態');
                    }
                })
                .catch(error => console.error('錯誤:', error));

            delete newMessages[notification.senderId];  // 點擊後刪除該發送者的通知
            displayNewMessages();  // 重新顯示剩下的通知
            updateMessageNotificationCount();  // 更新通知數量
        });

        messageNotificationList.appendChild(messageItem);  // 這裡直接使用 appendChild 保持排序
    });

    // 顯示或隱藏訊息通知列表
    if (sortedMessages.length > 0) {
        messageNotificationList.style.display = 'block';
    } else {
        messageNotificationList.style.display = 'none';
    }
}


function updateMessageNotificationCount() {
    const messageNotificationCount = document.getElementById('messageNotificationCount');
    const newCount = Object.keys(newMessages).length; // 根據 newMessages 的 senderId 數量來更新
    messageNotificationCount.textContent = newCount;

    // 如果有新訊息，顯示圖標數量；如果沒有，隱藏數量顯示
    if (newCount > 0) {
        messageNotificationCount.style.display = 'inline';
    } else {
        messageNotificationCount.style.display = 'none';
    }

    console.log('Updated message notification count to:', newCount);
}

// 搜尋聊天用戶
function searchChatUsers(username) {
    fetchUsers(username, currentUserId).then(users => {
        displayChatSearchResult(users);
    });
}

// 顯示搜尋聊天結果
function displayChatSearchResult(users) {
    const searchResultDiv = document.getElementById('userSearchResult');
    searchResultDiv.innerHTML = '';  // 清空現有結果
    if (users.length > 0) {
        users.forEach(user => {
            const userDiv = document.createElement('div');
            userDiv.classList.add('user-result-block');
            userDiv.setAttribute('data-user-id', user.id);
            userDiv.setAttribute('data-user-name', user.name);
            userDiv.innerHTML = `
                <img src="${defaultUserPhoto}" alt="${user.name}'s avatar" class="user-avatar" id="avatar-${user.id}">
                <span>${user.name}</span>
            `;
            searchResultDiv.appendChild(userDiv);
            // 非同步載入用戶大頭照
            loadUserAvatar(user.id).then(avatarUrl => {
                const avatarImg = document.getElementById(`avatar-${user.id}`);
                avatarImg.src = avatarUrl || defaultUserPhoto;
            });
        });
        searchResultDiv.style.display = 'block';
        // 綁定點擊用戶事件，開啟聊天室
        document.querySelectorAll('.user-result-block').forEach(userName => {
            userName.addEventListener('click', function () {
                const receiverId = this.getAttribute('data-user-id');
                const receiverName = this.getAttribute('data-user-name');
                checkChatRoom(receiverId).then(chatRoomId => {
                    console.log('checkChatRoom chatRoomId: ', chatRoomId)
                    openChatWindow(receiverName, receiverId, chatRoomId); // 打開兩人聊天室
                    searchResultDiv.style.display = 'none';
                });
            });
        });
    } else {
        searchResultDiv.innerHTML = '<p>無相符的使用者</p>';
    }
}


// 檢查聊天室是否存在，若無則生成
function checkChatRoom(receiverId) {
    return fetch(`/api/chat/chatroom?user1=${currentUserId}&user2=${receiverId}`)
        .then(response => response.json())
        .then(data => {
            console.log('checkChatRoom data: ', data)
            return data.chatRoomId;
        });
}

// 打開聊天視窗並訂閱聊天室
function openChatWindow(receiverName, receiverId, chatRoomId) {
    console.log('openChatWindow chatRoomId: ', chatRoomId)
    // 清空現有的聊天框內容
    const chatBox = document.getElementById('chatBox');
    chatBox.innerHTML = '';
    // 顯示聊天窗口
    const chatWindow = document.getElementById('chatWindow');
    const chatReceiver = document.getElementById('chatReceiver');
    chatReceiver.innerText = receiverName;
    chatReceiver.setAttribute('data-chatroom-id', chatRoomId);
    chatReceiver.setAttribute('data-receiver-id', receiverId);
    // 設置預設圖片
    const chatReceiverAvatar = document.getElementById('chatReceiverAvatar');
    chatReceiverAvatar.src = defaultUserPhoto;
    chatReceiverAvatar.setAttribute('data-receiver-id', receiverId);

    // 非同步載入用戶大頭照
    loadUserAvatar(receiverId).then(avatarUrl => {
        chatReceiverAvatar.src = avatarUrl || defaultUserPhoto;
    });
    chatWindow.style.display = 'block';
    // 訂閱對應的聊天室、載入歷史聊天紀錄
    subscribeToChatRoom(chatRoomId);
}

// 訂閱私人聊天室
function subscribeToChatRoom(chatRoomId) {
    if (subscribedChatRooms.has(chatRoomId)) {
        console.log('Already subscribed to chatRoom ' + chatRoomId);
    } else {
        console.log('Subscribing to chatRoom ' + chatRoomId);
        stompClient.subscribe(`/chat/private/${chatRoomId}`, function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            showMessage(message.senderName, message.senderId, message.content, message.createAt);
        });
        subscribedChatRooms.add(chatRoomId);
    }
    // 加載歷史聊天記錄
    loadChatHistory(chatRoomId);
}

// 動態顯示訊息
function showMessage(sender, senderId, message, createAt) {
    let chatBox = document.getElementById("chatBox");
    const newMessage = document.createElement('div');
    newMessage.classList.add('message');

    // 訊息內容容器
    const messageContentWrapper = document.createElement('div');
    messageContentWrapper.classList.add('message-content');

    // 訊息內容
    const messageContent = document.createElement('p');
    messageContent.textContent = message;

    // 將 UTC+0 的 createAt 轉換為 UTC+8 並格式化為 "上午/下午 HH:mm"
    const messageTimeText = formatTimeToAMPM(convertUTC0ToUTC8(createAt));

    // 訊息發送時間
    const messageTime = document.createElement('small');
    messageTime.textContent = messageTimeText; // 設置格式化後的時間
    if (senderId === currentUserId) {
        newMessage.classList.add('sender');
        messageContentWrapper.classList.add('sender-bg');
    } else {
        newMessage.classList.add('receiver');
        messageContentWrapper.classList.add('receiver-bg');
    }
    messageContentWrapper.appendChild(messageContent);
    newMessage.appendChild(messageContentWrapper);
    newMessage.appendChild(messageTime);

    chatBox.appendChild(newMessage);
    chatBox.scrollTop = chatBox.scrollHeight; // 自動滾動到聊天框底部
}


// 加載聊天歷史消息
function loadChatHistory(chatRoomId) {
    fetch(`/api/chat/chatroom/history?chatRoomId=${chatRoomId}`)
        .then(response => response.json())
        .then(data => {
            if (Array.isArray(data)) {
                data.forEach(message => {
                    console.log(message.senderName, message.content, message.createAt);
                    console.log('message time use Date: ', Date(message.createAt));
                    showMessage(message.senderName, message.senderId, message.content, message.createAt);
                });
            }
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
        });
}

// 發送訊息
function sendMessage() {
    const messageContent = document.getElementById('chatInput').value;
    const chatRoomId = document.getElementById('chatReceiver').getAttribute('data-chatroom-id');
    const receiverId = document.getElementById('chatReceiver').getAttribute('data-receiver-id');  // 取得 receiverId
    const receiverName = document.getElementById('chatReceiver').innerText;  // 取得 receiverName
    console.log('sendMessage - receiverId:', receiverId);
    console.log('sendMessage - receiverName:', receiverName);
    if (messageContent.trim() !== "") {
        const chatMessage = {
            chatRoomId: chatRoomId,
            content: messageContent,
            senderId: currentUserId,
            senderName: localStorage.getItem('currentUser'),
            receiverId: receiverId,
            receiverName: receiverName,
            createAt: getCurrentUTCTime()
        };
        stompClient.send("/app/notify/message", {}, JSON.stringify(chatMessage));
        document.getElementById('chatInput').value = '';  // 清空輸入框
    }
}

// 顯示好友圈
function showFriend(userProfile) {
    const friendsContainer = document.getElementById('friendsContainer');

    // 檢查好友是否已經在列表中
    let existingFriendDiv = friendsContainer.querySelector(`.friends[data-user-id="${userProfile.userId}"]`);
    if (!existingFriendDiv) {
        // 創建新的好友元素
        const friendDiv = document.createElement('div');
        friendDiv.classList.add('friends');
        friendDiv.setAttribute('data-user-id', userProfile.userId);
        console.log('userProfile', userProfile);
        friendDiv.innerHTML = `
            <img src="${userProfile.photo}" alt="${userProfile.username}" class="friend-avatar" onerror="this.src='${defaultUserPhoto}';">
            <span class="friend-name">${userProfile.username}</span>
        `;

        // 綁定點擊事件，點擊後開啟聊天室
        friendDiv.addEventListener('click', function () {
            const receiverId = userProfile.userId;
            const receiverName = userProfile.username;

            checkChatRoom(receiverId).then(chatRoomId => {
                console.log('checkChatRoom chatRoomId: ', chatRoomId);
                openChatWindow(receiverName, receiverId, chatRoomId); // 打開兩人聊天室
            });
        });

        friendsContainer.appendChild(friendDiv);
    }
}

function initializeChat() {
    currentUser = localStorage.getItem('currentUser');
    document.getElementById('UserName').textContent = `${currentUser}`;

    // 確保大頭照在資料獲取後設定
    loadUserAvatar(currentUserId).then(photoUrl => {
        const avatarElement = document.getElementById('currentUserAvatar');
        avatarElement.src = photoUrl;
        avatarElement.onclick = function () {
            window.location.href = `/profile.html?userId=${currentUserId}`;
        };
    });

    // 點擊圖示顯示/隱藏訊息通知列表
    document.getElementById('messageNotificationIcon').addEventListener('click', function () {
        const messageNotificationList = document.getElementById('messageNotificationList');
        messageNotificationList.style.display = messageNotificationList.style.display === 'none' ? 'block' : 'none';
    });

    document.getElementById('userSearchButton').addEventListener('click', function (event) {
        event.stopPropagation(); // 防止全局點擊事件觸發
        const username = document.getElementById('receiverSearch').value;
        if (username.trim() !== "") {
            searchChatUsers(username);
        }
    });


    // 點擊輸入框時，顯示搜尋結果（如果有內容）
    document.getElementById('receiverSearch').addEventListener('focus', function () {
        const username = this.value.trim();
        if (username !== "") {
            document.getElementById('userSearchResult').style.display = 'block';
        }
    });

    // 全局監聽點擊事件
    document.addEventListener('click', function (event) {
        const searchInput = document.getElementById('receiverSearch');
        const searchResult = document.getElementById('userSearchResult');
        const searchButton = document.getElementById('userSearchButton');

        // 如果點擊的地方不是搜尋框、結果列表或按鈕，就隱藏結果
        if (!searchInput.contains(event.target) && !searchResult.contains(event.target) && !searchButton.contains(event.target)) {
            searchResult.style.display = 'none'; // 隱藏搜尋結果
        }
    });

    // 綁定關閉按鈕的功能
    document.getElementById('closeChat').addEventListener('click', function () {
        const chatBox = document.getElementById('chatBox');
        chatBox.innerHTML = '';  // 清空聊天室內容
        const chatWindow = document.getElementById('chatWindow');
        chatWindow.style.display = 'none';
    });

    // 初始化載入好友區域
    fetch(`/api/users/friend?userId=${currentUserId}`)
        .then(response => response.json())
        .then(friendProfile => {
            friendProfile.forEach(profile => showFriend(profile));
        })
        .catch(error => console.error('Error:', error))

    // 初始化載入訊息通知
    fetch(`/api/chat/chat-request?userId=${currentUserId}`)
        .then(response => response.json())
        .then(chatNotify => {
            chatNotify.forEach(notify => displayChatNotify(notify));
        })
        .catch(error => console.error('Error:', error));
}

window.initializeChat = initializeChat;
window.displayChatNotify = displayChatNotify;
window.openChatWindow = openChatWindow;
window.showFriend = showFriend;
window.displayNewMessages = displayNewMessages;
window.updateMessageNotificationCount = updateMessageNotificationCount;
window.sendMessage = sendMessage;

if (typeof initializeApp === 'function') {
    initializeApp(isPost = true);
}


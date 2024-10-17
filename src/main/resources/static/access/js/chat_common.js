const subscribedChatRooms = new Set(); // 用於追蹤已訂閱的聊天室
const newMessages = {};
let hasLastPage = true;
let isLoading = false;
let lastCreateAt = null;

function displayChatNotify(notification) {
    const senderId = notification.senderId;
    newMessages[senderId] = notification;
    // 更新訊息通知數量
    updateMessageNotificationCount();
    displayNewMessages();
}

function displayNewMessages() {
    const messageNotificationList = document.getElementById('messageNotificationList');
    messageNotificationList.innerHTML = ''; // 清空現有的內容

    // 先將 newMessages 轉換為一個可以排序的 array
    const sortedMessages = Object.values(newMessages).sort((a, b) => {
        return new Date(b.createAt) - new Date(a.createAt);  // 按照 createAt 由大到小排序
    });

    sortedMessages.forEach(notification => {
        const messageItem = document.createElement('div');
        messageItem.classList.add('message-item');
        let senderName = notification.senderName;
        const content = notification.content;
        const timeDiff = displayTimeDifference(calculateTimeDifference(notification.createAt));
        const truncatedContent = content.length > 10 ? content.substring(0, 10) + '...' : content;

        messageItem.innerHTML = `
        <img src="${defaultUserPhoto}" alt="${senderName}" id="avatar-${notification.senderId}" class="user-avatar">
        <div class="message-content">
            <div class="message-header" id="sender-${notification.senderId}">
                <strong>${senderName}</strong>
                <span>${truncatedContent}</span>
            </div>
            <small>${timeDiff}</small>
        </div>
        `;
        loadUserAvatar(notification.senderId, true).then(data => {
            const {photo, username} = data;
            const avatarImg = document.getElementById(`avatar-${notification.senderId}`);
            avatarImg.src = photo || defaultUserPhoto;
            // 更新senderName
            const messageHeader = document.getElementById(`sender-${notification.senderId}`);
            messageHeader.querySelector('strong').innerText = senderName;
        })
        messageItem.addEventListener('click', function () {
            openChatWindow(notification.senderId, notification.chatRoomId);
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
    fetchFriends(username, currentUserId).then(users => {
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
            userDiv.setAttribute('data-user-id', user.friendId);
            userDiv.setAttribute('data-user-name', user.friendName);
            userDiv.innerHTML = `
                <img src="${defaultUserPhoto}" alt="${user.friendName}'s avatar" class="user-avatar" id="avatar-${user.friendId}">
                <span>${user.friendName}</span>
            `;
            searchResultDiv.appendChild(userDiv);
            // 非同步載入用戶大頭照
            loadUserAvatar(user.friendId).then(avatarUrl => {
                const avatarImg = document.getElementById(`avatar-${user.friendId}`);
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
                    openChatWindow(receiverId, chatRoomId);
                    searchResultDiv.style.display = 'none';
                });
            });
        });
    } else {
        searchResultDiv.innerHTML = '<p>無相符的使用者</p>';
        searchResultDiv.style.display = 'block';
    }
}


// 檢查聊天室是否存在，若無則生成
function checkChatRoom(receiverId) {
    return fetch(`/api/chat/chatroom?user1=${currentUserId}&user2=${receiverId}`)
        .then(response => response.json())
        .then(data => {
            return data.chatRoomId;
        });
}

// 打開聊天視窗並訂閱聊天室
function openChatWindow(receiverId, chatRoomId) {
    // 清空現有的聊天框內容
    const chatBox = document.getElementById('chatBox');
    chatBox.innerHTML = '';

    // 初始化分頁變數
    hasLastPage = true;
    isLoading = false;
    lastCreateAt = null;

    // 顯示聊天窗口
    const chatWindow = document.getElementById('chatWindow');
    const chatReceiver = document.getElementById('chatReceiver');
    chatReceiver.setAttribute('data-chatroom-id', chatRoomId);
    chatReceiver.setAttribute('data-receiver-id', receiverId);
    // 設置預設圖片
    const chatReceiverAvatar = document.getElementById('chatReceiverAvatar');
    chatReceiverAvatar.src = defaultUserPhoto;
    chatReceiverAvatar.setAttribute('data-receiver-id', receiverId);

    // 非同步載入用戶大頭照
    loadUserAvatar(receiverId, true).then(data => {
        const {photo, username} = data;
        chatReceiverAvatar.src = photo || defaultUserPhoto;
        chatReceiver.innerText = username;
    });
    chatWindow.style.display = 'block';
    // 訂閱對應的聊天室、載入歷史聊天紀錄
    subscribeToChatRoom(chatRoomId);
    initializeChatWindow();
}

// 訂閱私人聊天室
function subscribeToChatRoom(chatRoomId) {
    if (subscribedChatRooms.has(chatRoomId)) {
        console.log('Already subscribed to chatRoom ' + chatRoomId);
    } else {
        console.log('Subscribing to chatRoom ' + chatRoomId);
        stompClient.subscribe(`/chat/private/${chatRoomId}`, function (messageOutput) {
            const message = JSON.parse(messageOutput.body);
            showMessage(message.senderName, message.senderId, message.content, message.createAt, message.id);
        });
        subscribedChatRooms.add(chatRoomId);
    }
    // 加載歷史聊天記錄
    loadChatHistory(chatRoomId);
}

// 動態顯示訊息
function showMessage(sender, senderId, message, createAt, messageId, scrollToBottom = true) {
    let chatBox = document.getElementById("chatBox");
    if (document.querySelector(`[data-msg-id="${messageId}"]`)) {
        return; // 如果消息已存在,直接返回,不重複渲染
    }

    const newMessage = document.createElement('div');
    newMessage.classList.add('message');
    newMessage.setAttribute('data-msg-id', messageId);

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
    if (scrollToBottom) {
        chatBox.scrollTop = chatBox.scrollHeight; // 自動滾動到聊天框底部
    }
}

// 在chatBox頂部添加更舊的訊息（上一頁）
function prependMessage(sender, senderId, message, createAt, messageId) {
    let chatBox = document.getElementById("chatBox");
    if (document.querySelector(`[data-msg-id="${messageId}"]`)) {
        return; // 如果消息已存在,直接返回,不重複渲染
    }

    const newMessage = document.createElement('div');
    newMessage.classList.add('message');
    newMessage.setAttribute('data-msg-id', messageId);

    // 訊息內容容器
    const messageContentWrapper = document.createElement('div');
    messageContentWrapper.classList.add('message-content');

    // 訊息內容
    const messageContent = document.createElement('p');
    messageContent.textContent = message;
    // 格式化時間
    const messageTimeText = formatTimeToAMPM(convertUTC0ToUTC8(createAt));

    // 訊息發送時間
    const messageTime = document.createElement('small');
    messageTime.textContent = messageTimeText;

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

    // 在聊天框頂部插入訊息
    chatBox.prepend(newMessage);
}

// 初始化聊天視窗，監聽滾動事件
function initializeChatWindow() {
    const chatBox = document.getElementById('chatBox');
    chatBox.addEventListener('scroll', function () {
        if (chatBox.scrollTop === 0 && !isLoading && hasLastPage) {
            const chatRoomId = document.getElementById('chatReceiver').getAttribute('data-chatroom-id');
            const previousHeight = chatBox.scrollHeight;
            loadChatHistory(chatRoomId, lastCreateAt).then(() => {
                chatBox.scrollTop = chatBox.scrollHeight - previousHeight;
            });
        }
    });
}

// 加載聊天歷史消息
function loadChatHistory(chatRoomId, lastCreateAtParam = null) {
    return new Promise((resolve, reject) => {
        if (isLoading || !hasLastPage) {
            resolve();
            return;
        }
        isLoading = true;
        let url = `/api/chat/chatroom/history?chatRoomId=${chatRoomId}`;
        if (lastCreateAtParam) {
            url += `&lastCreateAt=${encodeURIComponent(lastCreateAtParam)}`;
        }

        fetch(url)
            .then(response => response.json())
            .then(data => {
                const messages = data.chatMessages;
                hasLastPage = data.hasLastPage;
                if (messages.length > 0) {
                    const chatBox = document.getElementById('chatBox');
                    if (lastCreateAtParam) {
                        // 載入更舊的訊息
                        const previousHeight = chatBox.scrollHeight;
                        messages.reverse().forEach(message => {
                            prependMessage(message.senderName, message.senderId, message.content, message.createAt, message.id);
                        });
                        chatBox.scrollTop = chatBox.scrollHeight - previousHeight;
                    } else {
                        // 初次載入
                        messages.forEach(message => {
                            showMessage(message.senderName, message.senderId, message.content, message.createAt, message.id, false);
                        });
                        // 滾動到聊天框底部
                        chatBox.scrollTop = chatBox.scrollHeight;
                    }
                    lastCreateAt = messages[0].createAt;
                }
                isLoading = false;
                resolve();
            })
            .catch(error => {
                console.error('Error loading chat history:', error);
                isLoading = false;
                reject(error);
            });
    });
}

// 發送訊息
function sendMessage() {
    const messageContent = document.getElementById('chatInput').value;
    const chatRoomId = document.getElementById('chatReceiver').getAttribute('data-chatroom-id');
    const receiverId = document.getElementById('chatReceiver').getAttribute('data-receiver-id');  // 取得 receiverId
    const receiverName = document.getElementById('chatReceiver').innerText;  // 取得 receiverName
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
        friendDiv.innerHTML = `
            <img src="${userProfile.photo}" alt="${userProfile.username}" class="friend-avatar" onerror="this.src='${defaultUserPhoto}';">
            <span class="friend-name">${userProfile.username}</span>
        `;

        // 綁定點擊事件，點擊後開啟聊天室
        friendDiv.addEventListener('click', function () {
            const receiverId = userProfile.userId;
            const receiverName = userProfile.username;

            checkChatRoom(receiverId).then(chatRoomId => {
                openChatWindow(receiverId, chatRoomId);
            });
        });

        friendsContainer.appendChild(friendDiv);
    }
}

window.displayChatNotify = displayChatNotify;
window.checkChatRoom = checkChatRoom;
window.openChatWindow = openChatWindow;
window.showFriend = showFriend;
window.displayNewMessages = displayNewMessages;
window.updateMessageNotificationCount = updateMessageNotificationCount;
window.sendMessage = sendMessage;
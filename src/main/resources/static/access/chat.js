let stompClient = null;
const urlParams = new URLSearchParams(window.location.search);
const currentUser = urlParams.get('username');
const subscribedChatRooms = new Set(); // 用於追蹤已訂閱的聊天室

// 初始化 WebSocket 連接
function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);

    stompClient.connect({ "username": currentUser }, function (frame) {
        console.log('Connected: ' + frame);

        // 訂閱公共頻道，用於接收聊天請求
        stompClient.subscribe('/chat-room/public', function (publicMessage) {
            console.log('publicMessage ' + publicMessage.body);
            const chatRequest = JSON.parse(publicMessage.body);

            // 確認訊息是發給當前用戶
            if (chatRequest.receiver === currentUser) {
                const chatRoomId = chatRequest.chatRoomId;

                // 檢查是否已訂閱該聊天室，避免重複訂閱(重複訂閱會怎樣嗎）？
                if (!subscribedChatRooms.has(chatRoomId)) {
                    // 訂閱私人聊天室
                    stompClient.subscribe('/chat-room/' + chatRoomId, function (messageOutput) {
                        const privateMessage = JSON.parse(messageOutput.body);
                        console.log("Message from: " + privateMessage.sender);
                        showMessage(privateMessage.sender, privateMessage.content, chatRoomId);
                    });
                    subscribedChatRooms.add(chatRoomId);

                    // 加載歷史消息
                    loadChatHistory(chatRoomId);
                }
            }
        });
    }, function (error) {
        console.error('STOMP connection error: ' + error);
    });
}

// 發送訊息
// 發送訊息時不手動顯示自己的訊息，交給 WebSocket 處理
function sendMessage() {
    const messageContent = document.getElementById('content').value;
    const receiverName = document.getElementById('receiver').value;

    if (messageContent.trim() !== "") {
        const chatRoomId = generateChatRoomId(currentUser, receiverName);
        // 有需要另外存一張表紀錄關係嗎？不能直接存到mongoDB?->避免每次都要重新編碼？
        checkChatRoomExist(chatRoomId).then(isExist => {
            if (!isExist) {
                saveChatRoomId(chatRoomId, receiverName);
            }
            sendChatRequest(chatRoomId, receiverName);
            subscribeToChatRoom(chatRoomId).then(() => {
                const chatMessage = {
                    chatRoomId: chatRoomId,
                    content: messageContent,
                    sender: currentUser,
                    receiver: receiverName,
                    type: 'CHAT'
                };

                // 傳送訊息
                stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));

                // 這裡不再手動顯示自己的訊息，等待 WebSocket 收到訊息後統一處理
                document.getElementById('content').value = '';
            });
        }).catch(error => {
            console.log(error);
            alert('發送訊息時發生錯誤。');
        });
    }
}


// 訂閱聊天室，確保雙方都已訂閱後再發送訊息
function subscribeToChatRoom(chatRoomId) {
    return new Promise((resolve, reject) => {
        // 確認當前用戶是否已訂閱該聊天室
        if (!subscribedChatRooms.has(chatRoomId)) {
            stompClient.subscribe('/chat-room/' + chatRoomId, function (messageOutput) {
                const message = JSON.parse(messageOutput.body);
                showMessage(message.sender, message.content, chatRoomId);
            });
            subscribedChatRooms.add(chatRoomId);

            // 加載歷史聊天記錄
            loadChatHistory(chatRoomId);
        }

        // 假設接收方會在收到聊天請求後訂閱聊天室，這裡可以等一小段時間
        setTimeout(() => {
            resolve();
        }, 500); // 等待訂閱完成
    });
}


// 發送聊天請求到公共頻道
function sendChatRequest(chatRoomId, receiverName) {
    const chatRequest = {
        chatRoomId: chatRoomId,
        receiver: receiverName
    };
    stompClient.send("/app/chat.sendRequest", {}, JSON.stringify(chatRequest));
}

// 檢查聊天室是否存在
function checkChatRoomExist(chatRoomId) {
    return fetch(`/chat/chatRoomExist?chatRoomId=${chatRoomId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            return response.json();
        })
        .then(data => {
            return data.exists;  // 從後端返回是否存在聊天室
        });
}

// 將聊天室 id 存到 DB 並訂閱聊天室
function saveChatRoomId(chatRoomId, receiverName) {
    fetch('/chat/createRoom', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ user1: currentUser, user2: receiverName })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to create chat room');
            }
            return response.json();
        })
        .then(data => {
            if (data.chatRoomId) {
                // 訂閱私人聊天室
                connectToChatRoom(data.chatRoomId);

                // 加載歷史消息
                loadChatHistory(data.chatRoomId);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('創建聊天室時發生錯誤。');
        });
}

// 訂閱私人聊天室
function connectToChatRoom(chatRoomId) {
    if (subscribedChatRooms.has(chatRoomId)) {
        console.log('Already subscribed to chatRoom ' + chatRoomId);
        return;
    }

    console.log('Subscribing to chatRoom ' + chatRoomId);
    stompClient.subscribe('/chat-room/' + chatRoomId, function (messageOutput) {
        const message = JSON.parse(messageOutput.body);
        showMessage(message.sender, message.content, chatRoomId);
    });
    subscribedChatRooms.add(chatRoomId);

    // 加載歷史消息，應在訂閱後立即加載
    loadChatHistory(chatRoomId);
}

// 根據用戶名生成聊天室 ID
function generateChatRoomId(user1, user2) {
    return [user1, user2].sort().join("-");
}

// 動態生成並顯示訊息
function showMessage(sender, message, chatRoomId) {
    let chatBox = document.getElementById("chatBox-" + chatRoomId);

    // 如果還沒有創建聊天區域，則創建
    if (!chatBox) {
        chatBox = document.createElement('div');
        chatBox.id = "chatBox-" + chatRoomId;
        chatBox.classList.add('chat-box'); // 添加樣式類別，可根據需要調整

        // 添加聊天對象的名稱作為標題
        const senderTitle = document.createElement("h3");
        senderTitle.textContent = `ChatRoom: ${chatRoomId}`;
        chatBox.appendChild(senderTitle);

        // 將聊天區域添加到頁面上
        document.getElementById("chatContainer").appendChild(chatBox);
    }

    // 添加新訊息到聊天區域
    const newMessage = document.createElement("div");
    newMessage.classList.add('message'); // 添加樣式類別，可根據需要調整
    newMessage.innerHTML = `<strong>${sender}:</strong> ${message}`;
    chatBox.appendChild(newMessage);

    // 滾動到最新消息
    chatBox.scrollTop = chatBox.scrollHeight;
}

// 加載聊天歷史消息
function loadChatHistory(chatRoomId) {
    fetch(`/chat/getChatHistory?chatRoomId=${chatRoomId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch chat history');
            }
            return response.json();
        })
        .then(data => {
            if (Array.isArray(data)) {
                data.forEach(message => {
                    showMessage(message.sender, message.content, chatRoomId);
                });
            }
        })
        .catch(error => {
            console.error('Error loading chat history:', error);
            alert('載入聊天歷史時發生錯誤。');
        });
}

// 初始化 WebSocket 連接
connect();

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
    fetchWithJwt(`/api/users/friend?userId=${currentUserId}`)
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

if (typeof initializeApp === 'function') {
    initializeApp(isPost = true);
}


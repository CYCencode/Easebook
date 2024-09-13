let stompClient = null;
const urlParams = new URLSearchParams(window.location.search);
const currentUser = urlParams.get('username');


function connect() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({"username": currentUser}, function (frame) {
        console.log('Connected: ' + frame);
        // 訂閱自己的頻道，才能收到各方傳來的訊息
        stompClient.subscribe('/user/queue/reply', function (messageOutput){
            const message = JSON.parse(messageOutput.body);
            console.log("Message from: "+ message.sender);
            // 那如果自己跟自己對話? 會顯示兩次
            if(message.sender===currentUser){
                showMessage(message.receiver, message.content);
            }else{
                showMessage(message.sender, message.content);
            }

        })

    });
}

function connectToChatRoom(chatRoomId) {
    stompClient.subscribe('/chat-room/' + chatRoomId, function (messageOutput) {
        const message = JSON.parse(messageOutput.body);  // 解析收到的消息
        console.log("Message from: " + message.sender);
        showMessage(message.sender , message.content);
    });
}

//用戶名稱會重複，是否有更好的識別方式？
function generateChatRoomId(user1, user2){
    return [user1, user2].sort().join("-");
}

function sendMessage() {
    const messageContent = document.getElementById('content').value;
    const receiverName = document.getElementById('receiver').value;
    if (messageContent.trim() !== "") {
        const chatRoomId = generateChatRoomId(currentUser, receiverName);
        const chatMessage = {
            chatRoomId: chatRoomId,  // 傳遞專屬的聊天ID
            content: messageContent,
            sender: currentUser,
            receiver: receiverName,
            type: 'CHAT'
        };
        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        // 清除聊天輸入框
        document.getElementById('content').value = '';
    }
}
// 動態生成分區消息顯示
function showMessage(chatWithUser, message){
    let chatBox = document.getElementById("chatBox-"+chatWithUser);
    // 檢查是否已經為該聊天對象創建聊天區域
    if(!chatBox){
        chatBox = document.createElement('div');
        chatBox.id = "chatBox-"+chatWithUser;
        // 創建標題顯示聊天對象
        const senderTitle = document.createElement("h3");
        senderTitle.textContent=chatWithUser;
        chatBox.appendChild(senderTitle);
        // 聊天區域新增到頁面上
        document.getElementById("chatContainer").appendChild(chatBox);
    }
    // 將新消息加到聊天區域
    const newMessage =document.createElement("div");
    newMessage.appendChild(document.createTextNode(message));
    chatBox.appendChild(newMessage);

}
// function showMessage(message) {
//     const chatBox = document.getElementById("chatBox");
//     const newMessage = document.createElement("div");
//     newMessage.appendChild(document.createTextNode(message));
//     chatBox.appendChild(newMessage);
// }

connect();

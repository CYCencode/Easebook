// post.js
let currentPage;
let loading;
let limit;
let hasMorePosts;

// 全域變數，用於新增貼文時的圖片和影片列表
// let imageFileList = [];
// let videoFileList = [];

function loadPosts(userId, page, limit) {
    loading = true; // 開始加載資料
    if (checkJwtToken()) {
        fetchWithJwt(`/api/posts?userId=${userId}&page=${page}&limit=${limit}`)
            .then(response => response.json())
            .then(data => {
                const posts = data.posts;
                const hasMore = data.hasMore;  // 從伺服器回傳的結果中取得 hasMore 屬性

                if (posts.length > 0) {
                    posts.forEach(post => displayPost(post, prepend = false));
                    console.log('第' + page + '頁，還有下一頁：' + hasMore);
                }
                // 更新 hasMorePosts 狀態
                hasMorePosts = hasMore;
                loading = false;  // 加載完畢，允許下一次加載
            })
            .catch(error => {
                if (error.status === 401) {
                    // JWT token 可能無效或過期，重導到登入頁面
                    redirectToLogin();
                } else {
                    console.error('Error:', error);
                }
                loading = false;
            })

    } else {
        // 沒有 JWT token，重導到登入頁面
        redirectToLogin();
    }
}

// 新增好友，貼文畫面更新
function fetchNewFriendPosts(friendId) {
    fetchWithJwt(`/api/posts/user/${friendId}?page=1&limit=1`)
        .then(response => response.json())
        .then(data => {
            const newFriendPosts = data.posts;
            newFriendPosts.forEach(post => {
                displayPost(post, prepend = true)
            });
        })
        .catch(error => console.error('Error fetching new friend posts:', error));
}

function initializePosts() {
    currentPage = 1;
    loading = false; // 防止重複加載
    limit = 5;
    hasMorePosts = true;

    // 初始化載入貼文資訊
    loadPosts(currentUserId, currentPage, limit);

    // 當滾動到底部時觸發事件
    window.addEventListener('scroll', (e) => {
        if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 100 && !loading && hasMorePosts) {
            currentPage++;
            loadPosts(currentUserId, currentPage, limit);
        }
    });
    // 登出邏輯
    document.getElementById('logoutButton').addEventListener('click', function () {
        // 清除 localStorage 中的用戶資訊
        localStorage.removeItem('jwtToken');
        localStorage.removeItem('currentUser');
        localStorage.removeItem('currentUserId');

        // 重定向到 login.html
        window.location.href = '/login.html';
    });

    // 監聽圖片和影片的選擇
    document.getElementById('media').addEventListener('change', function (event) {
        const previewContainer = document.getElementById('previewContainer'); // 對應預覽區域
        // 傳遞全域的 imageFileList 和 videoFileList，以及 existingFilesCount = 0
        handleFiles(event.target.files, previewContainer, imageFileList, videoFileList, 0); // 傳遞預覽區域和全域的檔案列表
    });
    // 監聽字數
    document.getElementById('postContent').addEventListener('input', function () {
        const content = this.value;
        const charCount = document.getElementById('charCount');
        charCount.textContent = `${content.length}/1000`;
        if (content.length > 1000) {
            // 顯示錯誤訊息
            alert("貼文字數長度不可超過1000字");
        }
    });


    // 發佈按鈕的處理
    document.getElementById('postButton').addEventListener('click', function () {
        const content = document.getElementById('postContent').value;
        const formData = new FormData();
        formData.append('userId', currentUserId); // 使用當前用戶ID
        formData.append('userName', localStorage.getItem('currentUser')); // 使用當前用戶名稱

        // 非同步取得頭像，並在取得後才發送POST請求
        loadUserAvatar(currentUserId).then(photoUrl => {
            formData.append('userPhoto', photoUrl); // 使用當前用戶頭像
            console.log("userPhoto: " + photoUrl);

            formData.append('content', content);

            // 加入圖、影片
            imageFileList.forEach(image => {
                formData.append('images', image);
            });
            videoFileList.forEach(video => {
                formData.append('videos', video);
            });

            // 加入 createAt 屬性，並將時間轉為 UTC+0 格式
            formData.append('createAt', getCurrentUTCTime());

            // 發送 POST 請求
            if (checkJwtToken()) {
                fetchWithJwt('/api/posts', {method: 'POST', body: formData})
                    .then(response => response.json())
                    .then(post => {
                        stompClient.send(`/app/notify/post`, {}, JSON.stringify(post));

                        // 即時顯示發佈的貼文
                        displayPost(post);

                        // 清空輸入欄位與預覽區
                        document.getElementById('postContent').value = '';
                        document.getElementById('previewContainer').innerHTML = '';
                        imageFileList = [];
                        videoFileList = [];
                        resetFileInput('media');
                        document.getElementById('charCount').textContent = '0/1000';
                    })
                    .catch(error => {
                        if (error.status === 401) {
                            // JWT token 可能無效或過期，重導到登入頁面
                            redirectToLogin();
                        } else {
                            console.error('Error:', error);
                        }
                    });
            } else {
                // 沒有 JWT token，重導到登入頁面
                redirectToLogin();
            }
        }).catch(error => {
            console.error('Error loading user avatar:', error);
        });
    });

}

window.fetchNewFriendPosts = fetchNewFriendPosts;
window.initializePosts = initializePosts;
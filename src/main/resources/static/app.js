// VibeNet State Management
const state = {
    token: localStorage.getItem('jwt_token') || '',
    username: localStorage.getItem('username') || '',
    profile: null,
    feed: [],
    feedPage: 0,
    feedSize: 5,
    hasMoreFeed: true,
    suggestedUsers: [],
    following: [],
    followers: [],
    isSearching: false,
    searchResults: { posts: [], users: [] },
    searchQuery: '',
    searchPage: 0,
    searchTab: 'posts' // 'posts' or 'users'
};

// API Endpoints Config
const API = {
    login: '/auth/login',
    register: '/auth/register',
    verify: '/auth/verify',
    user: '/user',
    feed: '/feed',
    post: '/post',
    like: '/like',
    comments: '/comments',
    follow: '/follow',
    search: '/search'
};

// Helper: Show Preloader
function showLoader(show = true) {
    const loader = document.getElementById('preloader');
    if (show) {
        loader.classList.remove('hidden');
    } else {
        loader.classList.add('hidden');
    }
}

// Helper: Show Toast Notification
function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${isError ? 'error' : ''}`;
    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 4000);
}

// Fetch helper with JWT inclusion
async function apiFetch(url, options = {}) {
    options.headers = options.headers || {};
    if (state.token) {
        options.headers['Authorization'] = `Bearer ${state.token}`;
    }
    
    try {
        const response = await fetch(url, options);
        if (response.status === 401 || response.status === 403) {
            // Token expired or invalid, force logout
            logout();
            showToast('Session expired. Please sign in again.', true);
            throw new Error('Unauthorized');
        }
        return response;
    } catch (err) {
        console.error(`API Fetch Error: ${url}`, err);
        throw err;
    }
}

// Initialize Application
document.addEventListener('DOMContentLoaded', async () => {
    setupEventListeners();
    
    if (state.token && state.username) {
        showLoader(true);
        try {
            await loadUserProfile();
            showAppView();
        } catch (err) {
            logout();
        } finally {
            showLoader(false);
        }
    } else {
        showAuthView();
    }
});

// Setup DOM Event Listeners
function setupEventListeners() {
    // Navigation toggle in Auth Card
    document.getElementById('go-to-register').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('login-form').classList.add('hidden');
        document.getElementById('register-form').classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Create your VibeNet account.';
    });

    document.getElementById('go-to-login').addEventListener('click', (e) => {
        e.preventDefault();
        document.getElementById('register-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Welcome back! Please login to your account.';
    });

    // Forms submission
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('compose-form').addEventListener('submit', handleComposePost);
    document.getElementById('edit-profile-form').addEventListener('submit', handleUpdateProfile);

    // Profile Edit Modal actions
    document.getElementById('edit-profile-btn').addEventListener('click', () => {
        if (!state.profile) return;
        document.getElementById('edit-name').value = state.profile.name || '';
        document.getElementById('edit-phone').value = state.profile.phone || '';
        document.getElementById('edit-location').value = state.profile.location || '';
        document.getElementById('edit-profile-pic').value = state.profile.profilePic || '';
        document.getElementById('edit-profile-modal').classList.remove('hidden');
    });

    document.querySelector('.close-modal').addEventListener('click', () => {
        document.getElementById('edit-profile-modal').classList.add('hidden');
    });

    // File picker display update
    document.getElementById('post-media-file').addEventListener('change', (e) => {
        const file = e.target.files[0];
        const label = document.getElementById('file-chosen-name');
        if (file) {
            label.textContent = file.name.length > 20 ? file.name.substring(0, 17) + '...' : file.name;
            label.style.color = 'var(--primary)';
        } else {
            label.textContent = 'Add Photo/Video';
            label.style.color = 'var(--text-muted)';
        }
    });

    // Visibility toggle
    document.getElementById('profile-visibility-toggle').addEventListener('change', async (e) => {
        try {
            await apiFetch(`${API.user}/visible`);
            showToast('Profile visibility updated.');
            if (state.profile) {
                state.profile.visible = e.target.checked;
            }
        } catch (err) {
            showToast('Failed to toggle visibility.', true);
        }
    });

    // Feed / Pagination
    document.getElementById('load-more-btn').addEventListener('click', () => {
        state.feedPage++;
        loadFeed(true);
    });

    // Search Operations
    document.getElementById('global-search-btn').addEventListener('click', triggerGlobalSearch);
    document.getElementById('global-search-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') triggerGlobalSearch();
    });
    document.getElementById('clear-search-btn').addEventListener('click', clearSearchResults);

    // Search Tabs toggling
    document.getElementById('tab-posts').addEventListener('click', () => {
        state.searchTab = 'posts';
        document.getElementById('tab-posts').classList.add('active');
        document.getElementById('tab-users').classList.remove('active');
        document.getElementById('search-posts-output').classList.remove('hidden');
        document.getElementById('search-users-output').classList.add('hidden');
    });

    document.getElementById('tab-users').addEventListener('click', () => {
        state.searchTab = 'users';
        document.getElementById('tab-users').classList.add('active');
        document.getElementById('tab-posts').classList.remove('active');
        document.getElementById('search-users-output').classList.remove('hidden');
        document.getElementById('search-posts-output').classList.add('hidden');
    });

    // Social list (Following / Followers) tabs
    document.getElementById('tab-following').addEventListener('click', () => {
        document.getElementById('tab-following').classList.add('active');
        document.getElementById('tab-followers').classList.remove('active');
        document.getElementById('following-list').classList.remove('hidden');
        document.getElementById('followers-list').classList.add('hidden');
    });

    document.getElementById('tab-followers').addEventListener('click', () => {
        document.getElementById('tab-followers').classList.add('active');
        document.getElementById('tab-following').classList.remove('active');
        document.getElementById('followers-list').classList.remove('hidden');
        document.getElementById('following-list').classList.add('hidden');
    });

    // Logout
    document.getElementById('logout-btn').addEventListener('click', logout);
}

// Show/Hide View functions
function showAuthView() {
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('app-section').classList.add('hidden');
}

async function showAppView() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('app-section').classList.remove('hidden');
    document.getElementById('header-user-display').textContent = `Hello, ${state.profile ? state.profile.name : state.username}`;
    
    // Set visibility toggle state
    if (state.profile) {
        document.getElementById('profile-visibility-toggle').checked = state.profile.visible;
        
        // Show/hide unverified banner
        const banner = document.getElementById('unverified-banner');
        if (state.profile.role === 'ROLE_UNVERIFIED') {
            banner.classList.remove('hidden');
            document.getElementById('composer-box').classList.add('hidden'); // unverified users can't post
        } else {
            banner.classList.add('hidden');
            document.getElementById('composer-box').classList.remove('hidden');
        }
    }

    renderSidebarProfile();
    
    // Load lists and timeline
    state.feedPage = 0;
    await Promise.all([
        loadFeed(),
        loadSuggestedUsers(),
        loadFollowingList(),
        loadFollowersList()
    ]);
}

// Log In Action
async function handleLogin(e) {
    e.preventDefault();
    const usernameInput = document.getElementById('login-username').value.trim();
    const passwordInput = document.getElementById('login-password').value;

    showLoader(true);
    try {
        const response = await fetch(API.login, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });

        if (!response.ok) {
            throw new Error('Invalid credentials');
        }

        const token = await response.text();
        state.token = token;
        state.username = usernameInput;
        localStorage.setItem('jwt_token', token);
        localStorage.setItem('username', usernameInput);

        await loadUserProfile();
        showToast('Successfully signed in!');
        await showAppView();
    } catch (err) {
        showToast('Login failed: Invalid username or password.', true);
    } finally {
        showLoader(false);
    }
}

// Register Action
async function handleRegister(e) {
    e.preventDefault();
    const userDto = {
        username: document.getElementById('reg-username').value.trim(),
        email: document.getElementById('reg-email').value.trim(),
        password: document.getElementById('reg-password').value,
        name: document.getElementById('reg-name').value.trim(),
        phone: document.getElementById('reg-phone').value.trim(),
        profilePic: document.getElementById('reg-profile-pic').value.trim() || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'
    };

    showLoader(true);
    try {
        const response = await fetch(API.register, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userDto)
        });

        if (!response.ok) {
            throw new Error('Registration failed');
        }

        showToast('Registration successful! Please check your email for verification.');
        document.getElementById('register-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Sign in to your new account.';
    } catch (err) {
        showToast('Registration failed. Username or email might be taken.', true);
    } finally {
        showLoader(false);
    }
}

// Logout Action
function logout() {
    state.token = '';
    state.username = '';
    state.profile = null;
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('username');
    showAuthView();
}

// Fetch Logged-in User Profile details
async function loadUserProfile() {
    // Spring Boot endpoint UserController does not have a direct profile endpoint.
    // However, we can fetch all users (which is allowed once authenticated) and filter by our username.
    const response = await apiFetch(API.user);
    if (!response.ok) throw new Error('Failed to load users');
    const users = await response.json();
    const matchingUser = users.find(u => u.username.toLowerCase() === state.username.toLowerCase());
    
    if (matchingUser) {
        state.profile = matchingUser;
    } else {
        throw new Error('Profile not found');
    }
}

// Render Sidebar Details
function renderSidebarProfile() {
    if (!state.profile) return;
    document.getElementById('profile-avatar').src = state.profile.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80';
    document.getElementById('profile-fullname').textContent = state.profile.name;
    document.getElementById('profile-handle').textContent = `@${state.profile.username}`;
    document.getElementById('profile-loc').textContent = state.profile.location || 'Planet Earth';
    document.getElementById('profile-phone').textContent = state.profile.phone || 'Not specified';
}

// Update Profile Details Action
async function handleUpdateProfile(e) {
    e.preventDefault();
    const updatedDto = {
        id: state.profile.id,
        name: document.getElementById('edit-name').value.trim(),
        phone: document.getElementById('edit-phone').value.trim(),
        location: document.getElementById('edit-location').value.trim(),
        profilePic: document.getElementById('edit-profile-pic').value.trim()
    };

    showLoader(true);
    try {
        const response = await apiFetch(API.user, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(updatedDto)
        });

        if (!response.ok) throw new Error('Update failed');
        const updatedProfile = await response.json();
        state.profile = { ...state.profile, ...updatedProfile };
        
        renderSidebarProfile();
        document.getElementById('header-user-display').textContent = `Hello, ${state.profile.name}`;
        document.getElementById('edit-profile-modal').classList.add('hidden');
        showToast('Profile updated successfully!');
    } catch (err) {
        showToast('Failed to update profile.', true);
    } finally {
        showLoader(false);
    }
}

// Load Feed Timeline
async function loadFeed(append = false) {
    if (!append) {
        state.feedPage = 0;
        state.feed = [];
    }

    try {
        const response = await apiFetch(`${API.feed}?page=${state.feedPage}&size=${state.feedSize}`);
        if (!response.ok) throw new Error('Failed to load feed');
        const data = await response.json();
        
        const posts = data.content || [];
        state.hasMoreFeed = !data.last;

        if (append) {
            state.feed = [...state.feed, ...posts];
        } else {
            state.feed = posts;
        }

        renderFeed(state.feed);

        const loadMoreBtn = document.getElementById('load-more-btn');
        if (state.hasMoreFeed) {
            loadMoreBtn.classList.remove('hidden');
        } else {
            loadMoreBtn.classList.add('hidden');
        }
    } catch (err) {
        showToast('Failed to load timeline.', true);
    }
}

// Render Feed Timeline List
function renderFeed(posts, elementId = 'feed-timeline') {
    const feedContainer = document.getElementById(elementId);
    if (!append && elementId === 'feed-timeline') {
        feedContainer.innerHTML = '';
    }

    if (posts.length === 0) {
        feedContainer.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted);">No vibes shared yet. Be the first!</div>`;
        return;
    }

    posts.forEach(post => {
        const dateStr = new Date(post.createdAt).toLocaleDateString(undefined, {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        const postCard = document.createElement('article');
        postCard.className = 'card post-card';
        postCard.id = `post-${post.id}`;

        let mediaHtml = '';
        if (post.mediaUrl) {
            if (post.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || post.mediaUrl.includes('video')) {
                mediaHtml = `<div class="post-media-container"><video src="${post.mediaUrl}" controls></video></div>`;
            } else {
                mediaHtml = `<div class="post-media-container"><img src="${post.mediaUrl}" alt="Post attachment"></div>`;
            }
        }

        postCard.innerHTML = `
            <div class="post-header">
                <div class="post-author-info">
                    <img class="post-avatar" src="https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80" alt="Avatar">
                    <div>
                        <div class="author-name">${post.username}</div>
                        <div class="post-timestamp">${dateStr}</div>
                    </div>
                </div>
            </div>
            <div class="post-content-text">${post.content}</div>
            ${mediaHtml}
            <div class="post-actions">
                <button class="action-btn like-btn" onclick="toggleLike(${post.id})">
                    ❤️ <span class="like-count">${post.likeCount}</span> Likes
                </button>
                <button class="action-btn comment-btn" onclick="toggleCommentsSection(${post.id})">
                    💬 <span>${post.commentsCount}</span> Comments
                </button>
            </div>
            <div id="comments-section-${post.id}" class="comments-section hidden">
                <div class="comments-list" id="comments-list-${post.id}"></div>
                <form class="comment-composer" onsubmit="handleAddComment(event, ${post.id})">
                    <input type="text" placeholder="Write a comment..." required>
                    <button type="submit" class="btn btn-primary btn-sm">Reply</button>
                </form>
            </div>
        `;

        feedContainer.appendChild(postCard);
    });
}

// Compile Post Composer Submission
async function handleComposePost(e) {
    e.preventDefault();
    const content = document.getElementById('post-content').value.trim();
    const mediaFile = document.getElementById('post-media-file').files[0];

    showLoader(true);
    try {
        const formData = new FormData();
        formData.append('content', content);
        if (mediaFile) {
            formData.append('file', mediaFile);
        }

        const response = await apiFetch(API.post, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error('Failed to create post');
        
        showToast('Vibe shared successfully!');
        document.getElementById('compose-form').reset();
        document.getElementById('file-chosen-name').textContent = 'Add Photo/Video';
        document.getElementById('file-chosen-name').style.color = 'var(--text-muted)';
        
        await loadFeed();
    } catch (err) {
        showToast('Error sharing post.', true);
    } finally {
        showLoader(false);
    }
}

// Toggle Like Action
async function toggleLike(postId) {
    try {
        const response = await apiFetch(`${API.like}/toggle?postId=${postId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Toggle like failed');
        const text = await response.text();
        
        // Find corresponding post DOM elements
        const postCard = document.getElementById(`post-${postId}`);
        if (postCard) {
            const likeSpan = postCard.querySelector('.like-count');
            let count = parseInt(likeSpan.textContent);
            if (text === 'Liked') {
                likeSpan.textContent = count + 1;
                postCard.querySelector('.like-btn').classList.add('liked');
            } else {
                likeSpan.textContent = Math.max(0, count - 1);
                postCard.querySelector('.like-btn').classList.remove('liked');
            }
        }
    } catch (err) {
        showToast('Error liking post.', true);
    }
}

// Fetch and Toggle Comments Drawer
async function toggleCommentsSection(postId) {
    const section = document.getElementById(`comments-section-${postId}`);
    if (!section.classList.contains('hidden')) {
        section.classList.add('hidden');
        return;
    }

    section.classList.remove('hidden');
    const list = document.getElementById(`comments-list-${postId}`);
    list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-light);">Loading comments...</div>`;

    try {
        const response = await apiFetch(`${API.comments}/post/${postId}`);
        if (!response.ok) throw new Error('Failed to load comments');
        const comments = await response.json();
        
        list.innerHTML = '';
        if (comments.length === 0) {
            list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-light); padding: 8px 0;">No replies yet.</div>`;
            return;
        }

        comments.forEach(comment => {
            const div = document.createElement('div');
            div.className = 'comment-item';
            div.innerHTML = `<span class="comment-author">${comment.user.username}:</span><span class="comment-text">${comment.comment}</span>`;
            list.appendChild(div);
        });
    } catch (err) {
        list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--primary); padding: 8px 0;">Error loading comments.</div>`;
    }
}

// Add Comment Submission
async function handleAddComment(e, postId) {
    e.preventDefault();
    const input = e.target.querySelector('input');
    const content = input.value.trim();
    if (!content) return;

    try {
        const response = await apiFetch(`${API.comments}?postId=${postId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
            body: content
        });

        if (!response.ok) throw new Error('Comment addition failed');
        input.value = '';

        // Reload comments section
        const list = document.getElementById(`comments-list-${postId}`);
        const responseList = await apiFetch(`${API.comments}/post/${postId}`);
        const comments = await responseList.json();
        
        list.innerHTML = '';
        comments.forEach(comment => {
            const div = document.createElement('div');
            div.className = 'comment-item';
            div.innerHTML = `<span class="comment-author">${comment.user.username}:</span><span class="comment-text">${comment.comment}</span>`;
            list.appendChild(div);
        });

        // Update comment counter in post card
        const postCard = document.getElementById(`post-${postId}`);
        if (postCard) {
            const cBtnSpan = postCard.querySelector('.comment-btn span');
            cBtnSpan.textContent = comments.length;
        }
    } catch (err) {
        showToast('Failed to add comment.', true);
    }
}

// Load Social Network: Suggested Users to Follow
async function loadSuggestedUsers() {
    const list = document.getElementById('suggested-users-list');
    list.innerHTML = 'Loading suggested users...';

    try {
        const response = await apiFetch(API.user);
        if (!response.ok) throw new Error('Failed to load suggested users');
        const users = await response.json();
        
        // Exclude current logged in user and existing followings
        const filtered = users.filter(u => 
            u.username.toLowerCase() !== state.username.toLowerCase()
        );

        list.innerHTML = '';
        if (filtered.length === 0) {
            list.innerHTML = '<div style="font-size: 0.85rem; color: var(--text-light);">No other users found.</div>';
            return;
        }

        filtered.forEach(user => {
            const div = document.createElement('div');
            div.className = 'user-item';
            div.innerHTML = `
                <div class="user-item-info">
                    <img class="user-item-avatar" src="${user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                    <div class="user-item-details">
                        <span class="user-item-name">${user.name}</span>
                        <span class="user-item-handle">@${user.username}</span>
                    </div>
                </div>
                <button class="btn btn-outline btn-sm" onclick="toggleFollow(${user.id}, this)">Follow</button>
            `;
            list.appendChild(div);
        });
    } catch (err) {
        list.innerHTML = 'Error loading suggested users.';
    }
}

// Load Social Network: Following list
async function loadFollowingList() {
    const list = document.getElementById('following-list');
    list.innerHTML = 'Loading following list...';

    try {
        const response = await apiFetch(`${API.follow}/following`);
        if (!response.ok) throw new Error('Failed to load following');
        const following = await response.json();
        state.following = following;

        list.innerHTML = '';
        if (following.length === 0) {
            list.innerHTML = '<div style="font-size: 0.85rem; color: var(--text-light); text-align: center; padding: 12px 0;">You are not following anyone.</div>';
            return;
        }

        following.forEach(user => {
            const div = document.createElement('div');
            div.className = 'user-item';
            div.innerHTML = `
                <div class="user-item-info">
                    <img class="user-item-avatar" src="${user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                    <div class="user-item-details">
                        <span class="user-item-name">${user.name || user.username}</span>
                        <span class="user-item-handle">@${user.username}</span>
                    </div>
                </div>
                <button class="btn btn-secondary btn-sm" onclick="toggleUnfollow(${user.id})">Unfollow</button>
            `;
            list.appendChild(div);
        });
    } catch (err) {
        list.innerHTML = 'Error loading following list.';
    }
}

// Load Social Network: Followers list
async function loadFollowersList() {
    const list = document.getElementById('followers-list');
    list.innerHTML = 'Loading followers list...';

    try {
        const response = await apiFetch(`${API.follow}/followers`);
        if (!response.ok) throw new Error('Failed to load followers');
        const followers = await response.json();
        state.followers = followers;

        list.innerHTML = '';
        if (followers.length === 0) {
            list.innerHTML = '<div style="font-size: 0.85rem; color: var(--text-light); text-align: center; padding: 12px 0;">No followers yet.</div>';
            return;
        }

        followers.forEach(user => {
            const div = document.createElement('div');
            div.className = 'user-item';
            div.innerHTML = `
                <div class="user-item-info">
                    <img class="user-item-avatar" src="${user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                    <div class="user-item-details">
                        <span class="user-item-name">${user.name || user.username}</span>
                        <span class="user-item-handle">@${user.username}</span>
                    </div>
                </div>
            `;
            list.appendChild(div);
        });
    } catch (err) {
        list.innerHTML = 'Error loading followers list.';
    }
}

// Toggle Follow on Suggested Users
async function toggleFollow(targetUserId, btnElement) {
    try {
        btnElement.disabled = true;
        const response = await apiFetch(`${API.follow}/${targetUserId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Follow toggle failed');
        showToast('Follow status updated!');
        
        await loadFollowingList();
    } catch (err) {
        showToast('Error toggling follow status.', true);
    } finally {
        btnElement.disabled = false;
    }
}

// Unfollow user from following list
async function toggleUnfollow(targetUserId) {
    try {
        const response = await apiFetch(`${API.follow}/${targetUserId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Unfollow failed');
        
        showToast('Unfollowed user.');
        await loadFollowingList();
        await loadSuggestedUsers();
        await loadFeed(); // Reload feed to remove unfollowed user's posts
    } catch (err) {
        showToast('Error unfollowing user.', true);
    }
}

// Global Search
async function triggerGlobalSearch() {
    const queryInput = document.getElementById('global-search-input').value.trim();
    if (!queryInput) return;

    state.isSearching = true;
    state.searchQuery = queryInput;
    state.searchPage = 0;

    const resultsBox = document.getElementById('search-results-section');
    resultsBox.classList.remove('hidden');

    const postsOutput = document.getElementById('search-posts-output');
    const usersOutput = document.getElementById('search-users-output');

    postsOutput.innerHTML = 'Searching vibes...';
    usersOutput.innerHTML = 'Searching profiles...';

    try {
        const response = await apiFetch(`${API.search}?search=${encodeURIComponent(queryInput)}&page=0&size=10&type=`);
        if (!response.ok) throw new Error('Search failed');
        const results = await response.json();
        
        // Render search posts
        const posts = results.posts ? results.posts.content : [];
        postsOutput.innerHTML = '';
        if (posts.length === 0) {
            postsOutput.innerHTML = '<div style="padding: 16px; color: var(--text-light);">No matching vibes found.</div>';
        } else {
            // Render inside search panel
            renderFeed(posts, 'search-posts-output');
        }

        // Render search users
        const users = results.users ? results.users.content : [];
        usersOutput.innerHTML = '';
        if (users.length === 0) {
            usersOutput.innerHTML = '<div style="padding: 16px; color: var(--text-light);">No matching people found.</div>';
        } else {
            users.forEach(user => {
                const div = document.createElement('div');
                div.className = 'user-item';
                div.innerHTML = `
                    <div class="user-item-info">
                        <img class="user-item-avatar" src="${user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                        <div class="user-item-details">
                            <span class="user-item-name">${user.name || user.username}</span>
                            <span class="user-item-handle">@${user.username}</span>
                        </div>
                    </div>
                `;
                usersOutput.appendChild(div);
            });
        }
    } catch (err) {
        showToast('Search request failed.', true);
        clearSearchResults();
    }
}

// Clear Search Results Panel
function clearSearchResults() {
    state.isSearching = false;
    state.searchQuery = '';
    document.getElementById('global-search-input').value = '';
    document.getElementById('search-results-section').classList.add('hidden');
}

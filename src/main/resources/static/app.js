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
    searchTab: 'posts',
    
    // Snapchat & Instagram Hybrid State
    activeStories: [],
    storyPlayer: {
        stories: [],
        currentIndex: 0,
        timer: null
    },
    exploreFeed: [],
    explorePage: 0,
    exploreSize: 12,
    streaks: [],
    currentChatUser: null,
    chatMessages: [],
    chatPollInterval: null
};

// API Configuration Mapping
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
    search: '/search',
    story: '/story',
    chat: '/chat'
};

// Helper: Show/Hide Preloader
function showLoader(show = true) {
    const loader = document.getElementById('preloader');
    if (show) loader.classList.remove('hidden');
    else loader.classList.add('hidden');
}

// Helper: Display Alert Toast
function showToast(message, isError = false) {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${isError ? 'error' : ''}`;
    toast.classList.remove('hidden');
    setTimeout(() => {
        toast.classList.add('hidden');
    }, 4500);
}

// Global Custom Fetch Wrap
async function apiFetch(url, options = {}) {
    options.headers = options.headers || {};
    if (state.token) {
        options.headers['Authorization'] = `Bearer ${state.token}`;
    }
    
    try {
        const response = await fetch(url, options);
        if (response.status === 401 || response.status === 403) {
            logout();
            showToast('Session expired. Please sign in.', true);
            throw new Error('Unauthorized');
        }
        return response;
    } catch (err) {
        console.error(`Fetch error on ${url}:`, err);
        throw err;
    }
}

// Start Application
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

// Setup Main Event Listeners
function setupEventListeners() {
    // Auth Toggle navigation
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
        document.getElementById('auth-subtitle').textContent = 'Welcome back! Please login.';
    });

    // Form Submissions
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('compose-form').addEventListener('submit', handleComposePost);
    document.getElementById('edit-profile-form').addEventListener('submit', handleUpdateProfile);

    // Profile Modals
    document.getElementById('edit-profile-btn').addEventListener('click', () => {
        if (!state.profile) return;
        document.getElementById('edit-name').value = state.profile.name || '';
        document.getElementById('edit-phone').value = state.profile.phone || '';
        document.getElementById('edit-location').value = state.profile.location || '';
        document.getElementById('edit-profile-pic').value = state.profile.profilePic || '';
        document.getElementById('edit-profile-modal').classList.remove('hidden');
    });

    // File input selection updates
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

    // Profile Visibility Toggle
    document.getElementById('profile-visibility-toggle').addEventListener('change', async (e) => {
        try {
            await apiFetch(`${API.user}/visible`);
            showToast('Profile visibility updated.');
            if (state.profile) state.profile.visible = e.target.checked;
        } catch (err) {
            showToast('Failed to toggle visibility.', true);
        }
    });

    // Home feed load more pagination
    document.getElementById('load-more-btn').addEventListener('click', () => {
        state.feedPage++;
        loadFeed(true);
    });

    // Search bar submit
    document.getElementById('global-search-btn').addEventListener('click', triggerGlobalSearch);
    document.getElementById('global-search-input').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') triggerGlobalSearch();
    });
    document.getElementById('clear-search-btn').addEventListener('click', clearSearchResults);

    // Sidebar View Workspace Toggles (Home, Explore, Chats)
    document.getElementById('nav-home-btn').addEventListener('click', () => toggleWorkspace('home'));
    document.getElementById('nav-explore-btn').addEventListener('click', () => toggleWorkspace('explore'));
    document.getElementById('nav-chats-btn').addEventListener('click', () => toggleWorkspace('chats'));
    document.getElementById('header-logo-btn').addEventListener('click', () => toggleWorkspace('home'));

    // Stories tray add story hook
    document.getElementById('self-story-btn').addEventListener('click', () => {
        document.getElementById('story-file-picker').click();
    });
    document.getElementById('story-file-picker').addEventListener('change', handleUploadStory);

    // Chat Window Composer Form
    document.getElementById('chat-composer-form').addEventListener('submit', handleSendChatMessage);
    document.getElementById('chat-media-file').addEventListener('change', (e) => {
        const file = e.target.files[0];
        const preview = document.getElementById('chat-file-preview-name');
        if (file) {
            preview.textContent = `📎 ${file.name.substring(0, 15)}`;
            preview.classList.remove('hidden');
        } else {
            preview.classList.add('hidden');
        }
    });

    // Search Tabs
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

    // Social sidepanel tabs
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

// Workspace switcher (Home Feed, Explore Photo Grid, Chat Messenger)
function toggleWorkspace(target) {
    // Nav active styling
    document.querySelectorAll('.nav-item').forEach(item => item.classList.remove('active'));
    document.getElementById(`nav-${target}-btn`).classList.add('active');

    // Display updates
    document.getElementById('workspace-home').classList.add('hidden');
    document.getElementById('workspace-explore').classList.add('hidden');
    document.getElementById('workspace-chats').classList.add('hidden');
    document.getElementById('stories-tray-box').classList.add('hidden');

    if (target === 'home') {
        document.getElementById('workspace-home').classList.remove('hidden');
        document.getElementById('stories-tray-box').classList.remove('hidden');
        loadFeed();
        loadStories();
    } else if (target === 'explore') {
        document.getElementById('workspace-explore').classList.remove('hidden');
        loadExploreGrid();
    } else if (target === 'chats') {
        document.getElementById('workspace-chats').classList.remove('hidden');
        loadChatStreaks();
    }
}

// Close Edit Modal helper
window.closeEditModal = function() {
    document.getElementById('edit-profile-modal').classList.add('hidden');
};

// Show/Hide authentication containers
function showAuthView() {
    document.getElementById('auth-section').classList.remove('hidden');
    document.getElementById('app-section').classList.add('hidden');
}

async function showAppView() {
    document.getElementById('auth-section').classList.add('hidden');
    document.getElementById('app-section').classList.remove('hidden');
    document.getElementById('header-user-display').textContent = `Hello, ${state.profile ? state.profile.name : state.username}`;
    document.getElementById('self-story-avatar').src = state.profile ? (state.profile.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80') : '';

    if (state.profile) {
        document.getElementById('profile-visibility-toggle').checked = state.profile.visible;
        
        // Show/hide unverified status banners
        const banner = document.getElementById('unverified-banner');
        if (state.profile.role === 'ROLE_UNVERIFIED') {
            banner.classList.remove('hidden');
            document.getElementById('composer-box').classList.add('hidden');
        } else {
            banner.classList.add('hidden');
            document.getElementById('composer-box').classList.remove('hidden');
        }
    }

    renderSidebarProfile();
    toggleWorkspace('home');
    
    // Parallel fetch social metadata
    await Promise.all([
        loadSuggestedUsers(),
        loadFollowingList(),
        loadFollowersList()
    ]);
}

// Authenticated Login Action
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

        if (!response.ok) throw new Error('Invalid credentials');

        const token = await response.text();
        state.token = token;
        state.username = usernameInput;
        localStorage.setItem('jwt_token', token);
        localStorage.setItem('username', usernameInput);

        await loadUserProfile();
        showToast('Successfully signed in!');
        await showAppView();
    } catch (err) {
        showToast('Authentication failed: Invalid credentials.', true);
    } finally {
        showLoader(false);
    }
}

// User Registration Action
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

        if (!response.ok) throw new Error('Registration failed');

        showToast('Register success! Check your email to verify account.');
        document.getElementById('register-form').classList.add('hidden');
        document.getElementById('login-form').classList.remove('hidden');
        document.getElementById('auth-subtitle').textContent = 'Sign in to your account.';
    } catch (err) {
        showToast('Register failed. Username/Email might be taken.', true);
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
    if (state.chatPollInterval) clearInterval(state.chatPollInterval);
    showAuthView();
}

// Fetch Profile detail from user list
async function loadUserProfile() {
    const response = await apiFetch(API.user);
    if (!response.ok) throw new Error('Failed to load users');
    const users = await response.json();
    const matching = users.find(u => u.username.toLowerCase() === state.username.toLowerCase());
    if (matching) {
        state.profile = matching;
    } else {
        throw new Error('Profile details could not be matched');
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

// Update Profile Form Action
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
        const updated = await response.json();
        state.profile = { ...state.profile, ...updated };
        
        renderSidebarProfile();
        document.getElementById('header-user-display').textContent = `Hello, ${state.profile.name}`;
        document.getElementById('edit-profile-modal').classList.add('hidden');
        showToast('Profile updated!');
    } catch (err) {
        showToast('Failed to update profile.', true);
    } finally {
        showLoader(false);
    }
}

// Load Home feed post timeline
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

        renderTimeline(state.feed);

        const loadBtn = document.getElementById('load-more-btn');
        if (state.hasMoreFeed) loadBtn.classList.remove('hidden');
        else loadBtn.classList.add('hidden');
    } catch (err) {
        showToast('Failed to load timeline.', true);
    }
}

// Draw timeline posts list
function renderTimeline(posts, containerId = 'feed-timeline') {
    const container = document.getElementById(containerId);
    if (containerId === 'feed-timeline' && state.feedPage === 0) {
        container.innerHTML = '';
    }

    if (posts.length === 0) {
        container.innerHTML = `<div class="card" style="text-align: center; color: var(--text-muted); padding: 30px;">Timeline is empty. Share your vibes!</div>`;
        return;
    }

    posts.forEach(post => {
        const dateStr = new Date(post.createdAt).toLocaleDateString(undefined, {
            month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
        });

        const postCard = document.createElement('article');
        postCard.className = 'card post-card';
        postCard.id = `${containerId}-post-${post.id}`;

        let mediaHtml = '';
        if (post.mediaUrl) {
            if (post.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || post.mediaUrl.includes('video')) {
                mediaHtml = `<div class="post-media-container"><video src="${post.mediaUrl}" controls></video></div>`;
            } else {
                mediaHtml = `<div class="post-media-container"><img src="${post.mediaUrl}" alt="Post media"></div>`;
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
                <button class="action-btn like-btn" onclick="toggleLike(${post.id}, '${containerId}')">
                    ❤️ <span class="like-count">${post.likeCount}</span> Likes
                </button>
                <button class="action-btn comment-btn" onclick="toggleCommentsSection(${post.id}, '${containerId}')">
                    💬 <span>${post.commentsCount}</span> Comments
                </button>
            </div>
            <div id="${containerId}-comments-section-${post.id}" class="comments-section hidden">
                <div class="comments-list" id="${containerId}-comments-list-${post.id}"></div>
                <form class="comment-composer" onsubmit="handleAddComment(event, ${post.id}, '${containerId}')">
                    <input type="text" placeholder="Write a comment..." required>
                    <button type="submit" class="btn btn-primary btn-sm">Reply</button>
                </form>
            </div>
        `;

        container.appendChild(postCard);
    });
}

// Compose new vibe
async function handleComposePost(e) {
    e.preventDefault();
    const content = document.getElementById('post-content').value.trim();
    const mediaFile = document.getElementById('post-media-file').files[0];

    showLoader(true);
    try {
        const formData = new FormData();
        formData.append('content', content);
        if (mediaFile) formData.append('file', mediaFile);

        const response = await apiFetch(API.post, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error('Create post failed');
        
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

// Toggle Like
async function toggleLike(postId, containerId = 'feed-timeline') {
    try {
        const response = await apiFetch(`${API.like}/toggle?postId=${postId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Like toggle failed');
        const text = await response.text();
        
        const postCard = document.getElementById(`${containerId}-post-${postId}`);
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

// Fetch and open comments drawer
async function toggleCommentsSection(postId, containerId = 'feed-timeline') {
    const section = document.getElementById(`${containerId}-comments-section-${postId}`);
    if (!section.classList.contains('hidden')) {
        section.classList.add('hidden');
        return;
    }

    section.classList.remove('hidden');
    const list = document.getElementById(`${containerId}-comments-list-${postId}`);
    list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-light); padding: 8px 0;">Loading comments...</div>`;

    try {
        const response = await apiFetch(`${API.comments}/post/${postId}`);
        if (!response.ok) throw new Error('Failed to load comments');
        const comments = await response.json();
        
        list.innerHTML = '';
        if (comments.length === 0) {
            list.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-light); padding: 8px 0;">No comments yet.</div>`;
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

// Add comment
async function handleAddComment(e, postId, containerId = 'feed-timeline') {
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

        if (!response.ok) throw new Error('Comment failed');
        input.value = '';

        // Reload comments lists
        const list = document.getElementById(`${containerId}-comments-list-${postId}`);
        const responseList = await apiFetch(`${API.comments}/post/${postId}`);
        const comments = await responseList.json();
        
        list.innerHTML = '';
        comments.forEach(comment => {
            const div = document.createElement('div');
            div.className = 'comment-item';
            div.innerHTML = `<span class="comment-author">${comment.user.username}:</span><span class="comment-text">${comment.comment}</span>`;
            list.appendChild(div);
        });

        // Update counts
        const postCard = document.getElementById(`${containerId}-post-${postId}`);
        if (postCard) {
            const cBtnSpan = postCard.querySelector('.comment-btn span');
            cBtnSpan.textContent = comments.length;
        }
    } catch (err) {
        showToast('Failed to comment.', true);
    }
}

// Suggested Users
async function loadSuggestedUsers() {
    const list = document.getElementById('suggested-users-list');
    list.innerHTML = 'Loading suggested users...';

    try {
        const response = await apiFetch(API.user);
        if (!response.ok) throw new Error('Failed to load suggested users');
        const users = await response.json();
        
        const filtered = users.filter(u => u.username.toLowerCase() !== state.username.toLowerCase());
        list.innerHTML = '';
        if (filtered.length === 0) {
            list.innerHTML = '<div style="font-size: 0.85rem; color: var(--text-light); text-align: center;">No suggested users.</div>';
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

// Following list
async function loadFollowingList() {
    const list = document.getElementById('following-list');
    list.innerHTML = 'Loading following...';

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

// Followers list
async function loadFollowersList() {
    const list = document.getElementById('followers-list');
    list.innerHTML = 'Loading followers...';

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

// Toggle follow suggested user
async function toggleFollow(targetUserId, btn) {
    try {
        btn.disabled = true;
        const response = await apiFetch(`${API.follow}/${targetUserId}`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Follow toggle failed');
        showToast('Follow status updated!');
        
        await loadFollowingList();
    } catch (err) {
        showToast('Error toggling follow status.', true);
    } finally {
        btn.disabled = false;
    }
}

// Unfollow user
async function toggleUnfollow(targetUserId) {
    try {
        const response = await apiFetch(`${API.follow}/${targetUserId}`, {
            method: 'POST'
        });
        if (!response.ok) throw new Error('Unfollow failed');
        
        showToast('Unfollowed user.');
        await loadFollowingList();
        await loadSuggestedUsers();
        if (document.getElementById('workspace-home').classList.contains('hidden') === false) {
            await loadFeed();
        }
    } catch (err) {
        showToast('Error unfollowing.', true);
    }
}

// Global search
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
        
        const posts = results.posts ? results.posts.content : [];
        postsOutput.innerHTML = '';
        if (posts.length === 0) {
            postsOutput.innerHTML = '<div style="padding: 16px; color: var(--text-light);">No matching vibes.</div>';
        } else {
            renderTimeline(posts, 'search-posts-output');
        }

        const users = results.users ? results.users.content : [];
        usersOutput.innerHTML = '';
        if (users.length === 0) {
            usersOutput.innerHTML = '<div style="padding: 16px; color: var(--text-light);">No matching people.</div>';
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
        showToast('Search failed.', true);
        clearSearchResults();
    }
}

// Clear search
function clearSearchResults() {
    state.isSearching = false;
    state.searchQuery = '';
    document.getElementById('global-search-input').value = '';
    document.getElementById('search-results-section').classList.add('hidden');
}


/* ========================================================
   HYBRID SOCIAL FEATURES: STORIES, MESSAGES, STREAKS, GRIDS
   ======================================================== */

// 1. Stories Tray Actions
async function loadStories() {
    const tray = document.getElementById('stories-container');
    tray.innerHTML = '';

    try {
        const response = await apiFetch(`${API.story}/active`);
        if (!response.ok) throw new Error('Failed to load stories');
        const stories = await response.json();
        
        // Group stories by user username
        const groups = {};
        stories.forEach(story => {
            const username = story.user.username;
            if (!groups[username]) {
                groups[username] = {
                    user: story.user,
                    items: []
                };
            }
            groups[username].items.push(story);
        });

        state.activeStories = Object.values(groups);

        if (state.activeStories.length === 0) {
            tray.innerHTML = `<div style="font-size: 0.75rem; color: var(--text-light); padding-left: 10px;">No stories to view yet.</div>`;
            return;
        }

        state.activeStories.forEach((group, index) => {
            const div = document.createElement('div');
            div.className = 'story-item';
            div.onclick = () => openStoryPlayer(index);
            
            div.innerHTML = `
                <div class="story-avatar-wrapper">
                    <img src="${group.user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                </div>
                <span>${group.user.name || group.user.username}</span>
            `;
            tray.appendChild(div);
        });
    } catch (err) {
        console.error('Stories error:', err);
    }
}

// Upload new Story
async function handleUploadStory(e) {
    const file = e.target.files[0];
    if (!file) return;

    showLoader(true);
    try {
        const formData = new FormData();
        formData.append('file', file);

        const response = await apiFetch(API.story, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error('Story creation failed');
        showToast('Story shared with friends!');
        e.target.value = '';
        await loadStories();
    } catch (err) {
        showToast('Failed to upload story.', true);
    } finally {
        showLoader(false);
    }
}

// Open Story Slideshow Player modal
window.openStoryPlayer = function(groupIndex) {
    const group = state.activeStories[groupIndex];
    if (!group || group.items.length === 0) return;

    state.storyPlayer.stories = group.items;
    state.storyPlayer.currentIndex = 0;

    const modal = document.getElementById('story-player-modal');
    modal.classList.remove('hidden');
    
    playStorySlide();
};

function playStorySlide() {
    const player = state.storyPlayer;
    if (player.currentIndex >= player.stories.length) {
        closeStoryPlayer();
        return;
    }

    const story = player.stories[player.currentIndex];
    
    // Set headers details
    document.getElementById('story-player-avatar').src = story.user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80';
    document.getElementById('story-player-username').textContent = story.user.name || story.user.username;
    
    const dateStr = new Date(story.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    document.getElementById('story-player-time').textContent = dateStr;

    // Render media slot
    const isVideo = story.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || story.mediaUrl.includes('video');
    const imageElement = document.getElementById('story-image-item');
    const videoElement = document.getElementById('story-video-item');

    if (isVideo) {
        imageElement.classList.add('hidden');
        videoElement.classList.remove('hidden');
        videoElement.src = story.mediaUrl;
        videoElement.play();
    } else {
        videoElement.classList.add('hidden');
        imageElement.classList.remove('hidden');
        imageElement.src = story.mediaUrl;
    }

    // Build top progress timers bars
    const progressContainer = document.getElementById('story-progress-bars');
    progressContainer.innerHTML = '';
    
    player.stories.forEach((_, idx) => {
        const slot = document.createElement('div');
        slot.className = 'story-progress-bar-slot';
        
        const fill = document.createElement('div');
        fill.className = 'story-progress-bar-fill';
        
        if (idx < player.currentIndex) fill.style.width = '100%';
        else if (idx === player.currentIndex) {
            // Animate progress bar fill over 5 seconds
            setTimeout(() => { fill.style.width = '100%'; fill.style.transitionDuration = '5000ms'; }, 50);
        } else fill.style.width = '0%';
        
        slot.appendChild(fill);
        progressContainer.appendChild(slot);
    });

    if (player.timer) clearTimeout(player.timer);
    
    // Proceed to next slide in 5 seconds
    player.timer = setTimeout(() => {
        player.currentIndex++;
        playStorySlide();
    }, 5000);
}

// Close Story Player
window.closeStoryPlayer = function() {
    const modal = document.getElementById('story-player-modal');
    modal.classList.add('hidden');
    const videoElement = document.getElementById('story-video-item');
    videoElement.pause();
    videoElement.src = '';
    
    if (state.storyPlayer.timer) {
        clearTimeout(state.storyPlayer.timer);
        state.storyPlayer.timer = null;
    }
};


// 2. Direct Messages & Snaps & Streaks (Snapchat style)
async function loadChatStreaks() {
    const list = document.getElementById('chat-thread-list');
    list.innerHTML = 'Loading threads...';

    try {
        // Fetch users and active streaks
        const [usersResponse, streaksResponse] = await Promise.all([
            apiFetch(API.user),
            apiFetch(`${API.chat}/streaks`)
        ]);

        if (!usersResponse.ok || !streaksResponse.ok) throw new Error('Data load failed');
        const users = await usersResponse.json();
        const streaks = await streaksResponse.json();

        // Map streaks by contact user ID for easy lookup
        const streakMap = {};
        streaks.forEach(s => {
            const contactId = (s.user1.id === state.profile.id) ? s.user2.id : s.user1.id;
            streakMap[contactId] = s.streakCount;
        });

        // Filter out logged in user
        const contacts = users.filter(u => u.username.toLowerCase() !== state.username.toLowerCase());
        
        list.innerHTML = '';
        if (contacts.length === 0) {
            list.innerHTML = '<div style="font-size: 0.85rem; color: var(--text-light); text-align: center; padding: 20px;">No friends list found. Add followers first!</div>';
            return;
        }

        contacts.forEach(user => {
            const streakVal = streakMap[user.id] || 0;
            const div = document.createElement('div');
            div.className = 'chat-thread-item';
            div.onclick = () => openChatWindow(user, streakVal);
            
            div.innerHTML = `
                <div class="user-item-info">
                    <img class="chat-thread-avatar" src="${user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80'}" alt="Avatar">
                    <div class="user-item-details">
                        <span class="user-item-name">${user.name || user.username}</span>
                        <span class="user-item-handle">@${user.username}</span>
                    </div>
                </div>
                ${streakVal > 0 ? `<span class="streak-pill">🔥 ${streakVal} Days</span>` : `<span style="font-size: 0.75rem; color: var(--text-light);">Start Streak</span>`}
            `;
            list.appendChild(div);
        });
    } catch (err) {
        list.innerHTML = 'Error loading chat threads.';
    }
}

// Open Chat Window modal
window.openChatWindow = function(user, streakCount = 0) {
    state.currentChatUser = user;
    
    // Header bindings
    document.getElementById('chat-window-avatar').src = user.profilePic || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=150&q=80';
    document.getElementById('chat-window-title').textContent = `Chat with ${user.name || user.username}`;
    document.getElementById('chat-window-streak').textContent = `🔥 ${streakCount} Days`;

    // Clear composer
    document.getElementById('chat-message-input').value = '';
    document.getElementById('snap-disappearing-toggle').checked = false;
    document.getElementById('chat-media-file').value = '';
    document.getElementById('chat-file-preview-name').classList.add('hidden');

    document.getElementById('chat-window-modal').classList.remove('hidden');

    // Poll chat history periodically every 3 seconds
    loadChatMessages();
    if (state.chatPollInterval) clearInterval(state.chatPollInterval);
    state.chatPollInterval = setInterval(loadChatMessages, 3000);
};

// Close Chat Window
window.closeChatWindow = function() {
    document.getElementById('chat-window-modal').classList.add('hidden');
    state.currentChatUser = null;
    if (state.chatPollInterval) {
        clearInterval(state.chatPollInterval);
        state.chatPollInterval = null;
    }
    loadChatStreaks(); // Reload chat list to sync updated streaks
};

// Fetch chat history messages
async function loadChatMessages() {
    if (!state.currentChatUser) return;
    const container = document.getElementById('chat-messages-output');
    
    try {
        const response = await apiFetch(`${API.chat}/${state.currentChatUser.id}`);
        if (!response.ok) throw new Error('History load failed');
        const list = await response.json();
        
        container.innerHTML = '';
        if (list.length === 0) {
            container.innerHTML = `<div style="text-align: center; font-size: 0.8rem; color: var(--text-light); padding: 12px 0;">Tap to send a direct message or snap!</div>`;
            return;
        }

        list.forEach(msg => {
            const isSenderSelf = msg.sender.username.toLowerCase() === state.username.toLowerCase();
            const bubble = document.createElement('div');
            
            if (msg.disappearing && !isSenderSelf) {
                // Snapchat disappearing SNAP bubble
                if (msg.readAt && new Date().getTime() - new Date(msg.readAt).getTime() > 10000) {
                    // Deleted/invisible Snap (not loaded anymore or expired)
                    return;
                }
                
                bubble.className = 'chat-bubble snap-lock';
                bubble.innerHTML = `⚡ View Once Snap (Tap)`;
                bubble.onclick = () => revealDisappearingSnap(msg, bubble);
            } else {
                // Standard chat message or self snap
                bubble.className = `chat-bubble ${isSenderSelf ? 'outgoing' : 'incoming'}`;
                
                let mediaElement = '';
                if (msg.mediaUrl) {
                    const isVideo = msg.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || msg.mediaUrl.includes('video');
                    if (isVideo) {
                        mediaElement = `<div class="post-media-container"><video src="${msg.mediaUrl}" controls style="max-height: 150px;"></video></div>`;
                    } else {
                        mediaElement = `<div class="post-media-container"><img src="${msg.mediaUrl}" alt="Media Snap" style="max-height: 150px; cursor: pointer;" onclick="window.open('${msg.mediaUrl}')"></div>`;
                    }
                }
                
                bubble.innerHTML = `
                    ${mediaElement}
                    <div>${msg.content || ''}</div>
                    ${msg.disappearing ? `<span style="font-size: 0.65rem; opacity: 0.7; display: block; text-align: right;">⚡ Snap</span>` : ''}
                `;
            }

            container.appendChild(bubble);
        });

        // Scroll to bottom
        container.scrollTop = container.scrollHeight;
    } catch (err) {
        console.error('Chat load error:', err);
    }
}

// Reveal disappearing snaps (disappear after 10 seconds of clicking)
function revealDisappearingSnap(msg, element) {
    element.className = 'chat-bubble incoming';
    element.onclick = null; // Unbind click listener
    
    let mediaElement = '';
    if (msg.mediaUrl) {
        const isVideo = msg.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || msg.mediaUrl.includes('video');
        if (isVideo) {
            mediaElement = `<div class="post-media-container"><video src="${msg.mediaUrl}" autoplay controls style="max-height: 200px;"></video></div>`;
        } else {
            mediaElement = `<div class="post-media-container"><img src="${msg.mediaUrl}" alt="Snap" style="max-height: 200px;"></div>`;
        }
    }
    
    element.innerHTML = `
        ${mediaElement}
        <div>${msg.content || ''}</div>
        <span style="font-size: 0.65rem; color: var(--primary); display: block; font-weight: 500; margin-top: 4px;">⚡ Snapped! Disappearing...</span>
    `;

    // Snap is expired and will be filtered out from future loads.
    // Locally trigger animation to fade out and hide in 8 seconds.
    setTimeout(() => {
        element.style.opacity = '0.3';
        element.style.transition = 'opacity 2000ms ease';
    }, 6000);

    setTimeout(() => {
        element.remove();
    }, 8000);
}

// Send chat message submission
async function handleSendChatMessage(e) {
    e.preventDefault();
    if (!state.currentChatUser) return;

    const content = document.getElementById('chat-message-input').value.trim();
    const mediaFile = document.getElementById('chat-media-file').files[0];
    const disappearing = document.getElementById('snap-disappearing-toggle').checked;

    if (!content && !mediaFile) return;

    try {
        const formData = new FormData();
        formData.append('recipientId', state.currentChatUser.id);
        formData.append('content', content);
        formData.append('disappearing', disappearing);
        if (mediaFile) formData.append('file', mediaFile);

        const response = await apiFetch(API.chat, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) throw new Error('Send message failed');
        
        // Reset composer inputs
        document.getElementById('chat-message-input').value = '';
        document.getElementById('chat-media-file').value = '';
        document.getElementById('chat-file-preview-name').classList.add('hidden');
        document.getElementById('snap-disappearing-toggle').checked = false;

        await loadChatMessages();
    } catch (err) {
        showToast('Failed to send message.', true);
    }
}


// 3. Instagram Explore Grid Layout
async function loadExploreGrid() {
    const grid = document.getElementById('explore-grid');
    grid.innerHTML = 'Loading explore grid...';

    try {
        const response = await apiFetch(`${API.feed}/explore?page=${state.explorePage}&size=${state.exploreSize}`);
        if (!response.ok) throw new Error('Explore feed failed');
        
        const data = await response.json();
        const posts = data.content || [];
        state.exploreFeed = posts;

        grid.innerHTML = '';
        if (posts.length === 0) {
            grid.innerHTML = '<div style="grid-column: 1/4; text-align: center; color: var(--text-muted); padding: 30px;">No public explore vibes found.</div>';
            return;
        }

        posts.forEach(post => {
            const div = document.createElement('div');
            div.className = 'explore-grid-item';
            div.onclick = () => openExplorePostModal(post);

            // Thumbnail selection (Use mediaUrl if available, fallback to user profile pic or text box)
            let thumbnailHtml = '';
            if (post.mediaUrl) {
                if (post.mediaUrl.match(/\.(mp4|webm|ogg)$/i) || post.mediaUrl.includes('video')) {
                    thumbnailHtml = `<video src="${post.mediaUrl}" muted style="width:100%; height:100%; object-fit:cover;"></video>`;
                } else {
                    thumbnailHtml = `<img src="${post.mediaUrl}" alt="Explore vibe">`;
                }
            } else {
                thumbnailHtml = `
                    <div style="background: linear-gradient(135deg, hsl(230, 80%, 90%), hsl(328, 80%, 90%)); width: 100%; height: 100%; padding: 16px; font-size: 0.8rem; overflow: hidden; display: flex; align-items: center; justify-content: center; font-style: italic;">
                        "${post.content.length > 50 ? post.content.substring(0, 47) + '...' : post.content}"
                    </div>
                `;
            }

            div.innerHTML = `
                ${thumbnailHtml}
                <div class="explore-grid-overlay">
                    <span class="grid-stat">❤️ ${post.likeCount}</span>
                    <span class="grid-stat">💬 ${post.commentsCount}</span>
                </div>
            `;
            grid.appendChild(div);
        });
    } catch (err) {
        grid.innerHTML = 'Error loading explore vibes.';
    }
}

// Open Explore Grid Detail Modal
window.openExplorePostModal = function(post) {
    const modal = document.getElementById('explore-post-modal');
    modal.classList.remove('hidden');
    
    // Clear and draw singular post card inside the modal wrapper
    const container = document.getElementById('explore-post-detail-container');
    container.innerHTML = '';
    
    // Draw inside modal using Timeline function with a custom container ID
    renderTimeline([post], 'explore-post-detail-container');
    toggleCommentsSection(post.id, 'explore-post-detail-container'); // Auto expand comments
};

// Close Explore Grid Modal
window.closeExplorePostModal = function() {
    document.getElementById('explore-post-modal').classList.add('hidden');
};

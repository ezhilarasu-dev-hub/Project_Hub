// Authentication & Session Management Module

const Auth = {
    TOKEN_KEY: 'projecthub_token',
    USER_KEY: 'projecthub_user',

    getToken() {
        return localStorage.getItem(this.TOKEN_KEY);
    },

    getCurrentUser() {
        const userStr = localStorage.getItem(this.USER_KEY);
        try {
            return userStr ? JSON.parse(userStr) : null;
        } catch (e) {
            return null;
        }
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    hasRole(expectedRole) {
        const user = this.getCurrentUser();
        return user && user.role === expectedRole;
    },

    saveAuth(authResponse) {
        localStorage.setItem(this.TOKEN_KEY, authResponse.token);
        const user = {
            id: authResponse.id,
            name: authResponse.name,
            email: authResponse.email,
            role: authResponse.role
        };
        localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    },

    logout() {
        localStorage.removeItem(this.TOKEN_KEY);
        localStorage.removeItem(this.USER_KEY);
        window.location.href = '/login.html';
    },

    async authenticatedFetch(url, options = {}) {
        const token = this.getToken();
        const headers = options.headers ? new Headers(options.headers) : new Headers();

        if (token) {
            headers.set('Authorization', `Bearer ${token}`);
        }

        const fetchOptions = {
            ...options,
            headers: headers
        };

        const response = await fetch(url, fetchOptions);

        if (response.status === 401) {
            this.logout();
            throw new Error('Session expired or unauthorized. Please log in again.');
        }

        return response;
    },

    requireRole(expectedRole) {
        if (!this.isAuthenticated()) {
            window.location.href = '/login.html';
            return false;
        }

        const user = this.getCurrentUser();
        if (!user || user.role !== expectedRole) {
            alert('Access Denied: You do not have permission to view this section.');
            if (user && user.role === 'FACULTY') {
                window.location.href = '/faculty/dashboard.html';
            } else {
                window.location.href = '/student/dashboard.html';
            }
            return false;
        }
        return true;
    },

    renderNavbar(activePage) {
        const navContainer = document.getElementById('navbar-container');
        if (!navContainer) return;

        const user = this.getCurrentUser();
        if (!user) return;

        const isFaculty = user.role === 'FACULTY';
        const roleClass = isFaculty ? 'role-faculty' : 'role-student';

        let linksHtml = '';
        if (isFaculty) {
            linksHtml = `
                <li><a href="/faculty/dashboard.html" class="${activePage === 'faculty-dashboard' ? 'active' : ''}">Dashboard</a></li>
                <li><a href="/faculty/projects.html" class="${activePage === 'faculty-projects' ? 'active' : ''}">All Projects</a></li>
            `;
        } else {
            linksHtml = `
                <li><a href="/student/dashboard.html" class="${activePage === 'student-dashboard' ? 'active' : ''}">Dashboard</a></li>
                <li><a href="/student/projects.html" class="${activePage === 'student-projects' ? 'active' : ''}">Explore Projects</a></li>
                <li><a href="/student/project-form.html" class="${activePage === 'student-submit' ? 'active' : ''}">Submit Project</a></li>
            `;
        }

        navContainer.innerHTML = `
            <nav class="navbar">
                <a href="${isFaculty ? '/faculty/dashboard.html' : '/student/dashboard.html'}" class="navbar-brand">
                    <span>Institution Project Hub</span>
                </a>
                <ul class="navbar-links">
                    ${linksHtml}
                </ul>
                <div class="user-info">
                    <span class="role-badge ${roleClass}">${user.role}</span>
                    <span>${user.name}</span>
                    <button class="btn btn-secondary btn-sm" onclick="Auth.logout()">Logout</button>
                </div>
            </nav>
        `;
    }
};

window.Auth = Auth;

// Shared Projects Utilities Module

const ProjectUtils = {
    formatDate(dateString) {
        if (!dateString) return '-';
        const d = new Date(dateString);
        return d.toLocaleDateString(undefined, {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    },

    formatFileSize(bytes) {
        if (!bytes || bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
    },

    escapeHtml(str) {
        if (!str) return '';
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    },

    getStatusBadge(status) {
        const safeStatus = this.escapeHtml(status || 'SUBMITTED');
        return `<span class="status-badge status-${safeStatus}">${safeStatus}</span>`;
    },

    getVisibilityBadge(visibility) {
        if (visibility === 'ALL_STUDENTS') {
            return `<span class="visibility-badge" title="Visible to all students and faculty">All Students</span>`;
        }
        return `<span class="visibility-badge" style="background-color: #fee2e2; color: #991b1b;" title="Restricted to Faculty">Faculty Only</span>`;
    }
};

window.ProjectUtils = ProjectUtils;

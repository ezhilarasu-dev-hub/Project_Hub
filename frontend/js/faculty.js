// Faculty Workflow Module

const FacultyApp = {

    async initDashboard() {
        if (!Auth.requireRole('FACULTY')) return;
        Auth.renderNavbar('faculty-dashboard');

        try {
            // 1. Fetch Statistics
            const statsResp = await Auth.authenticatedFetch('/api/faculty/statistics');
            if (statsResp.ok) {
                const stats = await statsResp.json();
                document.getElementById('stat-total-projects').textContent = stats.totalProjects;
                document.getElementById('stat-total-students').textContent = stats.totalStudents;
                document.getElementById('stat-submitted-projects').textContent = stats.submittedProjects;
                document.getElementById('stat-approved-projects').textContent = stats.approvedProjects;
                document.getElementById('stat-rejected-projects').textContent = stats.rejectedProjects;
            }

            // 2. Fetch Recent Submissions
            const projectsResp = await Auth.authenticatedFetch('/api/faculty/projects');
            if (projectsResp.ok) {
                const projects = await projectsResp.json();
                const tbody = document.getElementById('recent-projects-tbody');
                if (tbody) {
                    if (projects.length === 0) {
                        tbody.innerHTML = `<tr><td colspan="5" style="text-align: center; color: #64748b; padding: 2rem;">No student projects have been submitted yet.</td></tr>`;
                        return;
                    }

                    tbody.innerHTML = projects.slice(0, 7).map(p => `
                        <tr>
                            <td><strong><a href="/faculty/project-details.html?id=${p.id}" style="color: var(--primary); text-decoration: none;">${ProjectUtils.escapeHtml(p.title)}</a></strong></td>
                            <td>${ProjectUtils.escapeHtml(p.studentName)}</td>
                            <td>${ProjectUtils.formatDate(p.createdAt)}</td>
                            <td>${ProjectUtils.getStatusBadge(p.status)}</td>
                            <td>
                                <a href="/faculty/project-details.html?id=${p.id}" class="btn btn-secondary btn-sm">Review</a>
                            </td>
                        </tr>
                    `).join('');
                }
            }
        } catch (err) {
            console.error(err);
        }
    },

    async initProjectsList() {
        if (!Auth.requireRole('FACULTY')) return;
        Auth.renderNavbar('faculty-projects');

        const searchInput = document.getElementById('faculty-search-input');
        const statusFilter = document.getElementById('faculty-status-filter');

        const loadProjects = async () => {
            const search = searchInput ? searchInput.value.trim() : '';
            const status = statusFilter ? statusFilter.value : '';

            let queryParams = [];
            if (search) queryParams.push(`search=${encodeURIComponent(search)}`);
            if (status && status !== 'ALL') queryParams.push(`status=${encodeURIComponent(status)}`);

            const url = '/api/faculty/projects' + (queryParams.length > 0 ? '?' + queryParams.join('&') : '');

            try {
                const resp = await Auth.authenticatedFetch(url);
                if (!resp.ok) throw new Error('Failed to load projects');
                const projects = await resp.json();

                const tbody = document.getElementById('faculty-projects-tbody');
                if (tbody) {
                    if (projects.length === 0) {
                        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2rem;">No matching projects found.</td></tr>`;
                        return;
                    }

                    tbody.innerHTML = projects.map(p => `
                        <tr>
                            <td><strong><a href="/faculty/project-details.html?id=${p.id}" style="color: var(--primary); text-decoration: none;">${ProjectUtils.escapeHtml(p.title)}</a></strong></td>
                            <td>${ProjectUtils.escapeHtml(p.studentName)}<br><small style="color: #64748b;">${ProjectUtils.escapeHtml(p.studentEmail)}</small></td>
                            <td>${ProjectUtils.getVisibilityBadge(p.sourceCodeVisibility)}</td>
                            <td>${ProjectUtils.getStatusBadge(p.status)}</td>
                            <td>${ProjectUtils.formatDate(p.createdAt)}</td>
                            <td>
                                <a href="/faculty/project-details.html?id=${p.id}" class="btn btn-primary btn-sm">Review &amp; Status</a>
                            </td>
                        </tr>
                    `).join('');
                }
            } catch (err) {
                console.error(err);
            }
        };

        if (searchInput) searchInput.addEventListener('input', loadProjects);
        if (statusFilter) statusFilter.addEventListener('change', loadProjects);
        loadProjects();
    },

    async initProjectDetails() {
        if (!Auth.requireRole('FACULTY')) return;
        Auth.renderNavbar('faculty-projects');

        const urlParams = new URLSearchParams(window.location.search);
        const projectId = urlParams.get('id');
        if (!projectId) {
            window.location.href = '/faculty/projects.html';
            return;
        }

        try {
            const response = await Auth.authenticatedFetch(`/api/faculty/projects/${projectId}`);
            if (!response.ok) {
                document.getElementById('project-details-card').innerHTML = `
                    <div class="alert alert-danger">Project record could not be retrieved.</div>
                `;
                return;
            }

            const p = await response.json();

            document.getElementById('detail-title').textContent = p.title;
            document.getElementById('detail-student').textContent = p.studentName + ' (' + p.studentEmail + ')';
            document.getElementById('detail-status').innerHTML = ProjectUtils.getStatusBadge(p.status);
            document.getElementById('detail-created').textContent = ProjectUtils.formatDate(p.createdAt);
            document.getElementById('detail-updated').textContent = ProjectUtils.formatDate(p.updatedAt);
            document.getElementById('detail-description').textContent = p.description;

            // Visibility indicators
            document.getElementById('detail-src-vis').innerHTML = ProjectUtils.getVisibilityBadge(p.sourceCodeVisibility);
            document.getElementById('detail-dep-vis').innerHTML = ProjectUtils.getVisibilityBadge(p.deployedLinkVisibility);

            // Preselect current status in form
            const statusSelect = document.getElementById('project-status-select');
            if (statusSelect) {
                statusSelect.value = p.status;
            }

            // Deployed URL
            const deployedSection = document.getElementById('detail-deployed-section');
            if (p.deployedUrl) {
                deployedSection.innerHTML = `<a href="${ProjectUtils.escapeHtml(p.deployedUrl)}" target="_blank" rel="noopener" class="btn btn-secondary btn-sm">Open Student's Deployed App &rarr;</a>`;
            } else {
                deployedSection.innerHTML = `<span style="color: #94a3b8;">No link submitted.</span>`;
            }

            // Source Code File Download
            const downloadSection = document.getElementById('detail-download-section');
            if (p.hasSourceCode) {
                downloadSection.innerHTML = `
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <span><strong>${ProjectUtils.escapeHtml(p.sourceCodeFileName || 'source_code.zip')}</strong> (${ProjectUtils.formatFileSize(p.sourceCodeFileSize)})</span>
                        <button class="btn btn-primary btn-sm" onclick="FacultyApp.downloadCode(${p.id})">Download Source Code (.ZIP)</button>
                    </div>
                `;
            } else {
                downloadSection.innerHTML = `<span style="color: #94a3b8;">No source code file uploaded.</span>`;
            }

            // Status Update Handler
            const statusForm = document.getElementById('status-update-form');
            if (statusForm) {
                statusForm.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    const newStatus = statusSelect.value;
                    const alertElem = document.getElementById('status-update-alert');
                    alertElem.classList.add('d-none');

                    try {
                        const updateResp = await Auth.authenticatedFetch(`/api/faculty/projects/${projectId}/status`, {
                            method: 'PUT',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({ status: newStatus })
                        });

                        if (!updateResp.ok) throw new Error('Status update failed');
                        const updatedProject = await updateResp.json();

                        document.getElementById('detail-status').innerHTML = ProjectUtils.getStatusBadge(updatedProject.status);
                        alertElem.className = 'alert alert-success';
                        alertElem.textContent = `Project status successfully updated to ${updatedProject.status}.`;
                        alertElem.classList.remove('d-none');
                    } catch (err) {
                        alertElem.className = 'alert alert-danger';
                        alertElem.textContent = err.message || 'Error updating project status.';
                        alertElem.classList.remove('d-none');
                    }
                });
            }

        } catch (err) {
            console.error(err);
        }
    },

    async downloadCode(projectId) {
        try {
            const token = Auth.getToken();
            const response = await fetch(`/api/projects/${projectId}/download`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                alert('Download failed or you do not have permission.');
                return;
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `project_${projectId}_source.zip`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (e) {
            alert('Error downloading file: ' + e.message);
        }
    },

    async triggerReindex() {
        const btn = document.getElementById('reindex-btn');
        const alertBox = document.getElementById('reindex-alert');

        if (btn) {
            btn.disabled = true;
            btn.textContent = 'Processing senior_projects.txt...';
        }

        try {
            const resp = await Auth.authenticatedFetch('/api/rag/reindex', { method: 'POST' });
            const data = await resp.json();

            if (alertBox) {
                alertBox.className = 'alert alert-success';
                alertBox.textContent = `RAG Reindex completed! Processed and indexed ${data.documentsProcessed} senior project records into vector store.`;
                alertBox.classList.remove('d-none');
            } else {
                alert(`RAG Reindex Completed: ${data.documentsProcessed} documents processed.`);
            }
        } catch (e) {
            if (alertBox) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = 'Failed to reindex: ' + e.message;
                alertBox.classList.remove('d-none');
            } else {
                alert('Reindex error: ' + e.message);
            }
        } finally {
            if (btn) {
                btn.disabled = false;
                btn.textContent = 'Reindex Senior Projects (RAG)';
            }
        }
    }
};

window.FacultyApp = FacultyApp;

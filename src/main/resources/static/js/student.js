// Student Workflow Module

const StudentApp = {

    async initDashboard() {
        if (!Auth.requireRole('STUDENT')) return;
        Auth.renderNavbar('student-dashboard');

        const user = Auth.getCurrentUser();
        const welcomeElem = document.getElementById('student-welcome-name');
        if (welcomeElem && user) {
            welcomeElem.textContent = user.name;
        }

        try {
            const response = await Auth.authenticatedFetch('/api/projects/my');
            if (!response.ok) throw new Error('Failed to fetch projects');
            const projects = await response.json();

            // Render stats
            const totalCount = document.getElementById('my-total-projects');
            const approvedCount = document.getElementById('my-approved-projects');
            const submittedCount = document.getElementById('my-pending-projects');

            if (totalCount) totalCount.textContent = projects.length;
            if (approvedCount) approvedCount.textContent = projects.filter(p => p.status === 'APPROVED').length;
            if (submittedCount) submittedCount.textContent = projects.filter(p => p.status === 'SUBMITTED').length;

            // Render table
            const tbody = document.getElementById('my-recent-projects-tbody');
            if (tbody) {
                if (projects.length === 0) {
                    tbody.innerHTML = `<tr><td colspan="4" style="text-align: center; color: #64748b; padding: 2rem;">You have not submitted any projects yet. Click "Submit Project" above to get started.</td></tr>`;
                    return;
                }

                tbody.innerHTML = projects.slice(0, 5).map(p => `
                    <tr>
                        <td><strong><a href="/student/project-details.html?id=${p.id}" style="color: var(--primary); text-decoration: none;">${ProjectUtils.escapeHtml(p.title)}</a></strong></td>
                        <td>${ProjectUtils.getStatusBadge(p.status)}</td>
                        <td>${ProjectUtils.formatDate(p.createdAt)}</td>
                        <td>
                            <a href="/student/project-details.html?id=${p.id}" class="btn btn-secondary btn-sm">View</a>
                            <a href="/student/project-form.html?id=${p.id}" class="btn btn-secondary btn-sm">Edit</a>
                        </td>
                    </tr>
                `).join('');
            }
        } catch (err) {
            console.error(err);
        }
    },

    async initProjectsList() {
        if (!Auth.requireRole('STUDENT')) return;
        Auth.renderNavbar('student-projects');

        const filterSelect = document.getElementById('project-view-filter');
        const searchInput = document.getElementById('project-search-input');

        const loadList = async () => {
            const viewType = filterSelect ? filterSelect.value : 'my';
            const searchTerm = searchInput ? searchInput.value.toLowerCase().trim() : '';

            const url = viewType === 'my' ? '/api/projects/my' : '/api/projects';
            try {
                const response = await Auth.authenticatedFetch(url);
                if (!response.ok) throw new Error('Failed to load projects');
                let projects = await response.json();

                if (searchTerm) {
                    projects = projects.filter(p =>
                        p.title.toLowerCase().includes(searchTerm) ||
                        p.description.toLowerCase().includes(searchTerm)
                    );
                }

                const tbody = document.getElementById('projects-table-tbody');
                if (tbody) {
                    if (projects.length === 0) {
                        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: #64748b; padding: 2rem;">No projects found matching the criteria.</td></tr>`;
                        return;
                    }

                    tbody.innerHTML = projects.map(p => `
                        <tr>
                            <td><strong><a href="/student/project-details.html?id=${p.id}" style="color: var(--primary); text-decoration: none;">${ProjectUtils.escapeHtml(p.title)}</a></strong></td>
                            <td>${ProjectUtils.escapeHtml(p.studentName)}</td>
                            <td>${ProjectUtils.getVisibilityBadge(p.sourceCodeVisibility)}</td>
                            <td>${ProjectUtils.getStatusBadge(p.status)}</td>
                            <td>${ProjectUtils.formatDate(p.createdAt)}</td>
                            <td>
                                <a href="/student/project-details.html?id=${p.id}" class="btn btn-secondary btn-sm">Details</a>
                                ${p.owner ? `<a href="/student/project-form.html?id=${p.id}" class="btn btn-secondary btn-sm">Edit</a>` : ''}
                            </td>
                        </tr>
                    `).join('');
                }
            } catch (err) {
                console.error(err);
            }
        };

        if (filterSelect) filterSelect.addEventListener('change', loadList);
        if (searchInput) searchInput.addEventListener('input', loadList);
        loadList();
    },

    async initProjectForm() {
        if (!Auth.requireRole('STUDENT')) return;
        Auth.renderNavbar('student-submit');

        const urlParams = new URLSearchParams(window.location.search);
        const editId = urlParams.get('id');

        const form = document.getElementById('project-form');
        const formTitle = document.getElementById('form-heading');
        const submitBtn = document.getElementById('submit-btn');
        const alertBox = document.getElementById('form-alert');

        if (editId) {
            if (formTitle) formTitle.textContent = 'Edit Project';
            if (submitBtn) submitBtn.textContent = 'Save Changes';

            try {
                const response = await Auth.authenticatedFetch(`/api/projects/${editId}`);
                if (!response.ok) throw new Error('Failed to load project details for editing');
                const project = await response.json();

                if (!project.owner) {
                    alert('You cannot edit a project that you do not own.');
                    window.location.href = '/student/dashboard.html';
                    return;
                }

                document.getElementById('title').value = project.title || '';
                document.getElementById('description').value = project.description || '';
                document.getElementById('deployedUrl').value = project.deployedUrl || '';
                document.getElementById('sourceCodeVisibility').value = project.sourceCodeVisibility || 'FACULTY_ONLY';
                document.getElementById('deployedLinkVisibility').value = project.deployedLinkVisibility || 'ALL_STUDENTS';

                if (project.hasSourceCode) {
                    const fileNote = document.getElementById('current-file-note');
                    if (fileNote) {
                        fileNote.innerHTML = `Current file: <strong>${ProjectUtils.escapeHtml(project.sourceCodeFileName || 'source_code.zip')}</strong> (${ProjectUtils.formatFileSize(project.sourceCodeFileSize)}). Uploading a new file will replace it.`;
                        fileNote.classList.remove('d-none');
                    }
                }
            } catch (err) {
                console.error(err);
            }
        }

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            alertBox.classList.add('d-none');

            const title = document.getElementById('title').value.trim();
            const description = document.getElementById('description').value.trim();
            const deployedUrl = document.getElementById('deployedUrl').value.trim();
            const sourceCodeVisibility = document.getElementById('sourceCodeVisibility').value;
            const deployedLinkVisibility = document.getElementById('deployedLinkVisibility').value;
            const fileInput = document.getElementById('sourceCodeFile');

            if (!title || !description) {
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = 'Project Title and Description are required.';
                alertBox.classList.remove('d-none');
                return;
            }

            const formData = new FormData();
            formData.append('title', title);
            formData.append('description', description);
            if (deployedUrl) formData.append('deployedUrl', deployedUrl);
            formData.append('sourceCodeVisibility', sourceCodeVisibility);
            formData.append('deployedLinkVisibility', deployedLinkVisibility);

            if (fileInput.files.length > 0) {
                formData.append('file', fileInput.files[0]);
            }

            submitBtn.disabled = true;
            submitBtn.textContent = 'Submitting...';

            try {
                const endpoint = editId ? `/api/projects/${editId}` : '/api/projects';
                const method = editId ? 'PUT' : 'POST';

                const response = await Auth.authenticatedFetch(endpoint, {
                    method: method,
                    body: formData
                });

                submitBtn.disabled = false;
                submitBtn.textContent = editId ? 'Save Changes' : 'Submit Project';

                if (!response.ok) {
                    const errData = await response.json().catch(() => ({}));
                    alertBox.className = 'alert alert-danger';
                    alertBox.textContent = errData.message || 'Error saving project.';
                    alertBox.classList.remove('d-none');
                    return;
                }

                const result = await response.json();
                window.location.href = `/student/project-details.html?id=${result.id}`;

            } catch (err) {
                submitBtn.disabled = false;
                submitBtn.textContent = editId ? 'Save Changes' : 'Submit Project';
                alertBox.className = 'alert alert-danger';
                alertBox.textContent = err.message || 'Network error occurred.';
                alertBox.classList.remove('d-none');
            }
        });
    },

    async initProjectDetails() {
        if (!Auth.requireRole('STUDENT')) return;
        Auth.renderNavbar('student-projects');

        const urlParams = new URLSearchParams(window.location.search);
        const projectId = urlParams.get('id');
        if (!projectId) {
            window.location.href = '/student/dashboard.html';
            return;
        }

        try {
            const response = await Auth.authenticatedFetch(`/api/projects/${projectId}`);
            if (!response.ok) {
                document.getElementById('project-details-card').innerHTML = `
                    <div class="alert alert-danger">Project not found or you do not have permission to view it.</div>
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

            // Deployed URL section
            const deployedSection = document.getElementById('detail-deployed-section');
            if (p.canViewDeployedUrl && p.deployedUrl) {
                deployedSection.innerHTML = `<a href="${ProjectUtils.escapeHtml(p.deployedUrl)}" target="_blank" rel="noopener" class="btn btn-secondary btn-sm">Open Deployed App &rarr;</a>`;
            } else if (!p.canViewDeployedUrl) {
                deployedSection.innerHTML = `<span style="color: #94a3b8; font-style: italic;">Restricted (Faculty Only)</span>`;
            } else {
                deployedSection.innerHTML = `<span style="color: #94a3b8;">No link provided</span>`;
            }

            // Source Code Download section
            const downloadSection = document.getElementById('detail-download-section');
            if (p.hasSourceCode) {
                if (p.canDownloadSourceCode) {
                    downloadSection.innerHTML = `
                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                            <span><strong>${ProjectUtils.escapeHtml(p.sourceCodeFileName || 'source_code.zip')}</strong> (${ProjectUtils.formatFileSize(p.sourceCodeFileSize)})</span>
                            <button class="btn btn-primary btn-sm" onclick="StudentApp.downloadCode(${p.id})">Download Source Code (.ZIP)</button>
                        </div>
                    `;
                } else {
                    downloadSection.innerHTML = `<span style="color: #991b1b; background-color: #fee2e2; padding: 0.25rem 0.5rem; border-radius: 4px; font-size: 0.85rem;">Source Code Restricted to Faculty Only</span>`;
                }
            } else {
                downloadSection.innerHTML = `<span style="color: #94a3b8;">No source code file uploaded.</span>`;
            }

            // Edit button if owner
            const ownerActions = document.getElementById('owner-actions');
            if (p.owner && ownerActions) {
                ownerActions.innerHTML = `<a href="/student/project-form.html?id=${p.id}" class="btn btn-secondary">Edit This Project</a>`;
                ownerActions.classList.remove('d-none');
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
    }
};

window.StudentApp = StudentApp;

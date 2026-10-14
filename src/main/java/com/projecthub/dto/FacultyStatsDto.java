package com.projecthub.dto;

public class FacultyStatsDto {

    private long totalProjects;
    private long totalStudents;
    private long submittedProjects;
    private long approvedProjects;
    private long rejectedProjects;

    public FacultyStatsDto() {
    }

    public FacultyStatsDto(long totalProjects, long totalStudents, long submittedProjects, long approvedProjects, long rejectedProjects) {
        this.totalProjects = totalProjects;
        this.totalStudents = totalStudents;
        this.submittedProjects = submittedProjects;
        this.approvedProjects = approvedProjects;
        this.rejectedProjects = rejectedProjects;
    }

    public long getTotalProjects() {
        return totalProjects;
    }

    public void setTotalProjects(long totalProjects) {
        this.totalProjects = totalProjects;
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getSubmittedProjects() {
        return submittedProjects;
    }

    public void setSubmittedProjects(long submittedProjects) {
        this.submittedProjects = submittedProjects;
    }

    public long getApprovedProjects() {
        return approvedProjects;
    }

    public void setApprovedProjects(long approvedProjects) {
        this.approvedProjects = approvedProjects;
    }

    public long getRejectedProjects() {
        return rejectedProjects;
    }

    public void setRejectedProjects(long rejectedProjects) {
        this.rejectedProjects = rejectedProjects;
    }
}

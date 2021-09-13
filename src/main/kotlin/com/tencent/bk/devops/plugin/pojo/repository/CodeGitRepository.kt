package com.tencent.bk.devops.plugin.pojo.repository

data class CodeGitRepository(
    override val aliasName: String,
    override val url: String,
    override val credentialId: String,
    override val projectName: String,
    override val userName: String,
    val authType: RepoAuthType? = RepoAuthType.SSH,
    override val projectId: String?,
    override val repoHashId: String?
) : Repository {
    companion object {
        const val classType = "codeGit"
    }
}

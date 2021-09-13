package com.tencent.bk.devops.plugin.pojo.repository

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "@type")
@JsonSubTypes(
    JsonSubTypes.Type(value = CodeSvnRepository::class, name = CodeSvnRepository.classType),
    JsonSubTypes.Type(value = CodeGitRepository::class, name = CodeGitRepository.classType),
    JsonSubTypes.Type(value = CodeGitlabRepository::class, name = CodeGitlabRepository.classType),
    JsonSubTypes.Type(value = GithubRepository::class, name = GithubRepository.classType),
    JsonSubTypes.Type(value = CodeTGitRepository::class, name = CodeTGitRepository.classType)
)
interface Repository {
    val aliasName: String
    val url: String
    val credentialId: String
    val projectName: String
    val userName: String
    val projectId: String?
    val repoHashId: String?
}

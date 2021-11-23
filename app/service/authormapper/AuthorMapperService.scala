package service.authormapper

import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import service.authormapper.cache.PublishOneCache
import service.authormapper.mapper.{AkkaAuthorMapperExecutor, AuthorDocumentCreator, AuthorFolderCreator, AuthorFolderMapper}

import javax.inject.Inject

class AuthorMapperService @Inject()(swsSourceApi: SwsSourceApi,
                                    swsApi: SwsApi,
                                    accessTokenHandler: AccessTokenHandler,
                                    authorFolderMapper: AuthorFolderMapper,
                                    authorFolderCreator: AuthorFolderCreator,
                                    authorDocumentCreator: AuthorDocumentCreator,
                                    publishOneCache: PublishOneCache) {

  def map(swsQuery: String): Unit = {
    new AkkaAuthorMapperExecutor(swsSourceApi,
                                 swsApi,
                                 accessTokenHandler,
                                 authorFolderMapper,
                                 authorFolderCreator,
                                 authorDocumentCreator,
                                 publishOneCache).map(swsQuery)
  }

}

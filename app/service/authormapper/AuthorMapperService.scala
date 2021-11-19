package service.authormapper

import components.publishone.AccessTokenHandler
import components.sws.{SwsApi, SwsSourceApi}
import service.authormapper.mapper.{AkkaAuthorMapperExecutor, AuthorFolderMapper}

import javax.inject.Inject

class AuthorMapperService @Inject()(swsSourceApi: SwsSourceApi,
                                    swsApi: SwsApi,
                                    accessTokenHandler: AccessTokenHandler,
                                    authorFolderMapper: AuthorFolderMapper) {

  def map(swsQuery: String): Unit = {
    new AkkaAuthorMapperExecutor(swsSourceApi, swsApi, accessTokenHandler, authorFolderMapper).map(swsQuery)
  }

}

/*
 * Copyright (C) 2010 Florian Maul
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fmaul.android.cmis.repo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.dom4j.Document;
import org.dom4j.Element;

import android.app.Activity;
import android.app.Application;
import de.fmaul.android.cmis.Prefs;
import de.fmaul.android.cmis.utils.FeedUtils;
import de.fmaul.android.cmis.utils.HttpUtils;
import de.fmaul.android.cmis.utils.StorageUtils;

/**
 * @author Florian Maul
 */
public class CmisRepository {

	private final String feedRootCollection;
	private final String feedTypesCollection;
	private final String uriTemplateQuery;
	private final String uriTemplateTypeById;
	private final String repositoryUser;
	private final String repositoryPassword;
	private final String repositoryWorkspace;
	private String feedParams;
	private final Boolean useFeedParams;
	private final Application application;
	private final String repositoryName;
	private String repositoryUrl;
	
	
	/**
	 * Connects to a CMIS Repository with the given connection information FIXME
	 * References to Application should be removed with DI
	 * 
	 * @param repositoryUrl
	 *            The base ATOM feed URL of the CMIS repository
	 * @param user
	 *            The user name to login to the repository
	 * @param password
	 *            The password to login to the repository
	 */
	/*private CmisRepository(Application application, String repositoryUrl, String user, String password, String workspace) {
		this.application = application;
		this.repositoryUser = user;
		this.repositoryPassword = password;
		this.repositoryWorkspace = workspace;

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser, repositoryPassword);
		
		Element wsElement = FeedUtils.getWorkspace(doc, workspace);
		
		feedRootCollection = FeedUtils.getCollectionUrlFromRepoFeed("root", wsElement);
		feedTypesCollection = FeedUtils.getCollectionUrlFromRepoFeed("types", wsElement);
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed("query", wsElement);
		uriTemplateTypeById = FeedUtils.getUriTemplateFromRepoFeed("typebyid", wsElement);
		
	}*/
	
	private CmisRepository(Application application, Prefs pref) {
		this.application = application;
		this.repositoryUser = pref.getUser();
		this.repositoryPassword = pref.getPassword();
		this.repositoryWorkspace = pref.getWorkspace();
		this.repositoryName = pref.getRepoName();
		this.repositoryUrl = pref.getUrl();

		Document doc = FeedUtils.readAtomFeed(repositoryUrl, repositoryUser, repositoryPassword);
		
		Element wsElement = FeedUtils.getWorkspace(doc, repositoryWorkspace);
		
		feedRootCollection = FeedUtils.getCollectionUrlFromRepoFeed("root", wsElement);
		feedTypesCollection = FeedUtils.getCollectionUrlFromRepoFeed("types", wsElement);
		uriTemplateQuery = FeedUtils.getUriTemplateFromRepoFeed("query", wsElement);
		uriTemplateTypeById = FeedUtils.getUriTemplateFromRepoFeed("typebyid", wsElement);
		feedParams = createParams(pref);
		useFeedParams = pref.getParams();
	}

	public String getFeedRootCollection() {
		return feedRootCollection;
	}

	public void setFeedParams(String feedParams) {
		this.feedParams = feedParams;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	/**
	 * Creates a repository connection from the application preferences.
	 * 
	 * @param prefs
	 * @return
	 */
	public static CmisRepository create(Application app, final Prefs prefs) {
		return new CmisRepository(app, prefs);
	}
	
	/**
	 * Returns the root collection with documents and folders.
	 * 
	 * @return
	 */
	public CmisItemCollection getRootCollection() {
		return getCollectionFromFeed(feedRootCollection);
	}
	
	public CmisItemCollection getRootCollection(String params) {
		return getCollectionFromFeed(feedRootCollection + params);
	}

	/**
	 * Returns the ATOM feed that can be used to perform a search for the
	 * specified search terms.
	 * 
	 * @param queryType
	 *            A {@link QueryType} that specifies the type of search.
	 * @param query
	 *            A query that will be run against the repository.
	 * @return
	 */
	public String getSearchFeed(QueryType queryType, String query) {
		switch (queryType) {
		case TITLE:
			return FeedUtils.getSearchQueryFeedTitle(uriTemplateQuery, query);
		case CMISQUERY:
			return FeedUtils.getSearchQueryFeedCmisQuery(uriTemplateQuery, query);
		case FULLTEXT:
		default:
			return FeedUtils.getSearchQueryFeedFullText(uriTemplateQuery, query);
		}
	}

	/**
	 * Returns the collection of {@link CmisItem}s for a given feed url from the
	 * CMIS repository.
	 * 
	 * @param feedUrl
	 * @return
	 */
	public CmisItemCollection getCollectionFromFeed(final String feedUrl) {
		Document doc;

		if (StorageUtils.isFeedInCache(application, feedUrl, repositoryWorkspace)) {
			doc = StorageUtils.getFeedFromCache(application, feedUrl, repositoryWorkspace);
		} else {
			doc = FeedUtils.readAtomFeed(feedUrl, repositoryUser, repositoryPassword);
			if (doc != null) {
				StorageUtils.storeFeedInCache(application, feedUrl, doc, repositoryWorkspace);
			}
		}
		return CmisItemCollection.createFromFeed(doc);
	}

	public CmisTypeDefinition getTypeDefinition(String documentTypeId) {
		String url = uriTemplateTypeById.replace("{id}", documentTypeId);
		Document doc = FeedUtils.readAtomFeed(url, repositoryUser, repositoryPassword);
		return CmisTypeDefinition.createFromFeed(doc);
	}

	public File retreiveContent(CmisItem item) {
		File contentFile = StorageUtils.getStorageFile(repositoryWorkspace, StorageUtils.TYPE_CONTENT, item.getId(), item.getTitle());

		try {
			contentFile.getParentFile().mkdirs();
			contentFile.createNewFile();
			OutputStream os = new BufferedOutputStream(new FileOutputStream(contentFile));

			downloadContent(item, os);
			os.close();
			return contentFile;
		} catch (Exception e) {

		}
		return null;
	}

	/**
	 * Fetches the contents from the CMIS repository for the given
	 * {@link CmisItem}.
	 */
	private void downloadContent(CmisItem item, OutputStream os) throws ClientProtocolException, IOException {
		HttpUtils.getWebRessource(item.getContentUrl(), repositoryUser, repositoryPassword).getEntity().writeTo(os);
	}

	public void clearCache(String workspace) {
		StorageUtils.deleteRepositoryFiles(application, workspace);
	}
	
	public String getRepositoryWorkspace() {
		return repositoryWorkspace;
	}
	
	public void reCreateParams(Activity activity){
		setFeedParams(createParams(new Prefs(activity)));
	}
	
	private String createParams(Prefs pref){
		String params = "";
		String value = "";
		ArrayList<String> listParams = new ArrayList<String>(4);
		
		if (pref != null){
			
			value = pref.getTypes();
			if (value != null && value.length() > 0){
				listParams.add("types" + "=" +  pref.getTypes());
			}
			
			/*
			if (pref.getFilter() != null){
				paramsList.put("filter", pref.getFilter());
			}*/
			
			value = pref.getMaxItems();
			if (value != null && value.length() > 0 && value.equals("-1") == false){
				listParams.add("maxItems" + "=" + pref.getMaxItems());
			}
			
			value = pref.getOrder() ;
			if (pref.getOrder() != null && pref.getOrder().length() > 0){
				listParams.add("orderBy" + "=" + pref.getOrder());
			}
			
			//TODO BETTER!
			for (String param : listParams) {
				params += "&" + param;
			}
			
			if (params.length() != 0){
				params = params.replaceFirst("&", "");
				params = "?" + params;
			} 
		}
		
		try {
			params = new URI(null, params, null).toASCIIString();
		} catch (URISyntaxException e) {
		}
		
		return params;
	}
	
	public String getFeedParams() {
		return feedParams;
	}

	public Boolean getUseFeedParams() {
		if (useFeedParams != null){
			return useFeedParams;
		} else {
			return false;
		}
		
	}
	
	public String getRepositoryName() {
		return repositoryName;
	}
	
	public String getRepositoryUser() {
		return repositoryUser;
	}

	public String getRepositoryPassword() {
		return repositoryPassword;
	}

}

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
package de.fmaul.android.cmis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SimpleAdapter;
import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisProperty;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.repo.CmisTypeDefinition;
import de.fmaul.android.cmis.utils.ActionUtils;
import de.fmaul.android.cmis.utils.IntentIntegrator;
import de.fmaul.android.cmis.utils.ListUtils;

public class DocumentDetailsActivity extends ListActivity {

	private CmisItem item;
	private Button download, share, edit, delete, qrcode;
	private String objectTypeId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.document_details_main);
		setTitleFromIntent();
		displayPropertiesFromIntent();
		displayActionIcons();
	}
	
	private void displayActionIcons(){
		
		item = CmisItem.create(getIntent().getStringExtra("title"), null, getIntent().getStringExtra("mimetype"), getIntent().getStringExtra("contentUrl"), getSelfUrlFromIntent());
		
		download = (Button) findViewById(R.id.download);
		share = (Button) findViewById(R.id.share);
		edit = (Button) findViewById(R.id.editmetadata);
		delete = (Button) findViewById(R.id.delete);
		qrcode = (Button) findViewById(R.id.qrcode);
		
		//File
		if (item != null && getContentFromIntent() != null){
			download.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					ActionUtils.openDocument(DocumentDetailsActivity.this, item);
				}
			});
			
			qrcode.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					IntentIntegrator.shareText(DocumentDetailsActivity.this, DocumentDetailsActivity.this.getIntent().getStringExtra("self"));
				}
			});
			
			edit.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			
		} else {
			//FOLDER
			download.setVisibility(View.GONE);
			edit.setVisibility(View.GONE);
			//share.setVisibility(View.GONE);
			//qrcode.setVisibility(View.GONE);
			delete.setVisibility(View.GONE);
		}
		
		
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ActionUtils.shareDocument(DocumentDetailsActivity.this, DocumentDetailsActivity.this.getIntent().getStringExtra("workspace"), item);
			}
		});
	}

	private void setTitleFromIntent() {
		String title = getIntent().getStringExtra("title");
		setTitle(getString(R.string.title_details) + " '" + title + "'");
	}

	private void displayPropertiesFromIntent() {
		List<CmisProperty> propList = getPropertiesFromIntent();
		objectTypeId = getObjectTypeIdFromIntent();
		CmisTypeDefinition typeDefinition = getRepository().getTypeDefinition(objectTypeId);
		List<Map<String, ?>> list = buildListOfNameValueMaps(propList, typeDefinition);
		initListAdapter(list);
	}

	private String getObjectTypeIdFromIntent() {
		return getIntent().getStringExtra("objectTypeId");
	}
	
	private String getBaseTypeIdFromIntent() {
		return getIntent().getStringExtra("baseTypeId");
	}
	
	private String getContentFromIntent() {
		return getIntent().getStringExtra("contentStream");
	}
	
	private String getSelfUrlFromIntent() {
		return getIntent().getStringExtra("self");
	}

	private void initListAdapter(List<Map<String, ?>> list) {
		SimpleAdapter props = new SimpleAdapter(this, list, R.layout.document_details_row, new String[] { "name", "value" }, new int[] {
				R.id.propertyName, R.id.propertyValue });

		setListAdapter(props);
	}

	private List<Map<String, ?>> buildListOfNameValueMaps(List<CmisProperty> propList, CmisTypeDefinition typeDefinition) {
		List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
		for (CmisProperty cmisProperty : propList) {
			if (cmisProperty.getDefinitionId() != null) {
				list.add(ListUtils.createPair(getDisplayNameFromProperty(cmisProperty, typeDefinition), cmisProperty.getValue()));
			}
		}
		return list;
	}

	private String getDisplayNameFromProperty(CmisProperty property, CmisTypeDefinition typeDefinition) {
		String name = property.getDisplayName();

		if (TextUtils.isEmpty(name)) {
		}
		name = typeDefinition.getDisplayNameForProperty(property);

		if (TextUtils.isEmpty(name)) {
			name = property.getDefinitionId();
		}
		
		return name.replaceAll("cmis:", "");
	}

	private ArrayList<CmisProperty> getPropertiesFromIntent() {
		ArrayList<CmisProperty> propList = getIntent().getParcelableArrayListExtra("properties");
		return propList;
	}

	CmisRepository getRepository() {
		return ((CmisApp) getApplication()).getRepository();
	}
	
/*
	private void openDocument() {

		File content = item.getContent(getIntent().getStringExtra("workspace"));
		if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent())){
			viewFileInAssociatedApp(content, item.getMimeType());
		} else {
			new AbstractDownloadTask(getRepository(), this) {
				@Override
				public void onDownloadFinished(File contentFile) {
					if (contentFile != null && contentFile.exists()) {
						viewFileInAssociatedApp(contentFile, item.getMimeType());
					} else {
						displayError(R.string.error_file_does_not_exists);
					}
				}
			}.execute(item);
		}
	}

	private void displayError(int messageId) {
		Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
	}
	

	private void viewFileInAssociatedApp(File tempFile, String mimeType) {
		Intent viewIntent = new Intent(Intent.ACTION_VIEW);
		Uri data = Uri.fromFile(tempFile);
		viewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		viewIntent.setDataAndType(data, mimeType.toLowerCase());

		try {
			startActivity(viewIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
	}
	
	private void shareDocument() {
		
		File content = item.getContent(getIntent().getStringExtra("workspace"));
		if (item.getMimeType().length() == 0){
			shareFileInAssociatedApp(content);
		} else if (content != null && content.length() > 0 && content.length() == Long.parseLong(getContentFromIntent())) {
			shareFileInAssociatedApp(content);
		} else {
			new AbstractDownloadTask(getRepository(), this) {
				@Override
				public void onDownloadFinished(File contentFile) {
						shareFileInAssociatedApp(contentFile);
				}
			}.execute(item);
		}
	}
	
	private void shareFileInAssociatedApp(File contentFile) {
		Intent i = new Intent(Intent.ACTION_SEND);
		i.putExtra(Intent.EXTRA_SUBJECT, item.getTitle());
		//item.get
		if (contentFile != null && contentFile.exists()){
			i.putExtra(Intent.EXTRA_TEXT, item.getContentUrl());
			i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(contentFile));
			i.setType(item.getMimeType());
		} else {
			i.putExtra(Intent.EXTRA_TEXT, getSelfUrlFromIntent());
			i.setType("plain/text");
		}
		startActivity(Intent.createChooser(i, "Send mail..."));
	}*/
	

}

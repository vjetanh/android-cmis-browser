package de.fmaul.android.cmis.asynctask;

import android.app.Application;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import de.fmaul.android.cmis.CmisApp;
import de.fmaul.android.cmis.ListCmisFeedActivity;
import de.fmaul.android.cmis.R;
import de.fmaul.android.cmis.model.Server;
import de.fmaul.android.cmis.repo.CmisRepository;
import de.fmaul.android.cmis.utils.FeedLoadException;

public class ServerInitTask extends AsyncTask<String, Void, CmisRepository> {

	private final ListCmisFeedActivity activity;
	private ProgressDialog pg;
	private Server server;
	private Application app;

	public ServerInitTask(ListCmisFeedActivity activity, Application app, final Server server) {
		super();
		this.activity = activity;
		this.app = app;
		this.server = server;
		
	}

	@Override
	protected void onPreExecute() {
		pg = ProgressDialog.show(activity, "", activity.getText(R.string.loading), true);
	}

	@Override
	protected CmisRepository doInBackground(String... params) {
		try {
			return CmisRepository.create(app, server);
		} catch (FeedLoadException fle) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(CmisRepository repo) {
		repo.generateParams(activity);
		((CmisApp) activity.getApplication()).setRepository(repo);
		((CmisApp) activity.getApplication()).getRepository().clearCache(repo.getServer().getWorkspace());
		activity.processSearchOrDisplayIntent();
		pg.dismiss();
	}

	@Override
	protected void onCancelled() {
		pg.dismiss();
	}
}
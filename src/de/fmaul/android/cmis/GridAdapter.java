package de.fmaul.android.cmis;

import de.fmaul.android.cmis.repo.CmisItem;
import de.fmaul.android.cmis.repo.CmisItemCollection;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class GridAdapter extends ArrayAdapter<CmisItem> {
    private Activity activity;
    
    static private class ViewHolder {
		TextView topText;
		ImageView icon;
	}

    
    public GridAdapter(Activity activity, int textViewResourceId, CmisItemCollection itemCollection) {
    	super(activity, textViewResourceId, itemCollection.getItems());
		this.activity = activity;
	}
    
    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
    	View v = recycleOrCreateView(convertView);
		ViewHolder vh = (ViewHolder) v.getTag();

		CmisItem item = getItem(position);
		updateControls(vh, item);
		return v;
    }
    
    
    private View recycleOrCreateView(View v) {
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.feed_grid_row, null);

			ViewHolder vh = new ViewHolder();
			vh.icon = (ImageView) v.findViewById(R.id.icon);
			vh.topText = (TextView) v.findViewById(R.id.toptext);

			v.setTag(vh);
		}
		return v;
	}
    
    private void updateControls(ViewHolder v, CmisItem item) {
		if (item != null) {
			v.topText.setText(item.getTitle());
			updateControlIcon(v, item);
		}
	}
    
    private void updateControlIcon(ViewHolder vh, CmisItem item) {

		if (item.hasChildren()) {
			vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.folderopen));
		} else {
			String mimetype = item.getMimeType();
			if(mimetype.contains("image")){
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.image));
			} else if(mimetype.contains("pdf")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.pdf));
			} else if(mimetype.contains("msword")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msword));
			} else if(mimetype.contains("excel")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.msexcel));
			} else if(mimetype.contains("point")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.mspowerpoint));
			} else if(mimetype.contains("html")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.html));
			} else if(mimetype.contains("video")) {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.video));
			} else {
				vh.icon.setImageDrawable(getContext().getResources().getDrawable(R.drawable.text));
			}
		}
	}
    
}
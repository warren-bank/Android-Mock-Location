package lib.folderpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FolderAdapter extends ArrayAdapter<FilePojo> {

	/**
	 *
	 */
	private class FileViewHolder {
		ImageView iconView;
		TextView nameView;
	}

	private LayoutInflater mInflater;
	private Context mContext;
	private ArrayList<FilePojo> dataList;

	public FolderAdapter(Context context, ArrayList<FilePojo> dataList) {
		super(context, R.layout.fp_filerow, dataList);
		this.mContext = context;
		this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.dataList = dataList;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new FileViewHolder();
			convertView = mInflater.inflate(R.layout.fp_filerow, null);
			viewHolder.iconView = convertView.findViewById(R.id.fp_iv_icon);
			viewHolder.nameView = convertView.findViewById(R.id.fp_tv_name);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (FileViewHolder) convertView.getTag();
		}

		FilePojo item = dataList.get(position);
		int iconRes = (item.isFolder()) ? R.drawable.fp_folder : R.drawable.fp_file;
		viewHolder.iconView.setImageResource(iconRes);
		viewHolder.nameView.setText(item.getName());

		return convertView;
	}

}
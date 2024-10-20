package project1.com.accountawakedrive;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageSpinnerAdapter extends ArrayAdapter<String> {

    private Context context;
    private int[] images;  // Mảng chứa ID của các hình ảnh
    private String[] texts;  // Mảng chứa tên các mục

    public ImageSpinnerAdapter(Context context, String[] texts, int[] images) {
        super(context, R.layout.spinner_item_with_image, texts);
        this.context = context;
        this.images = images;
        this.texts = texts;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return createCustomView(position, convertView, parent);
    }

    private View createCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.spinner_item_with_image, parent, false);

        ImageView imageView = row.findViewById(R.id.image);
//        TextView textView = row.findViewById(R.id.text);

        imageView.setImageResource(images[position]);  // Hiển thị hình ảnh
//        textView.setText(texts[position]);  // Hiển thị văn bản

        return row;
    }
}

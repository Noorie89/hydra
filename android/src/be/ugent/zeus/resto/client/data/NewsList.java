package be.ugent.zeus.resto.client.data;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import be.ugent.zeus.resto.client.R;
import java.util.Comparator;
import java.util.List;

public class NewsList extends ArrayAdapter<NewsItem> {

  /**
   * The length of the article previews, in number of characters.
   */
  private static final int SHORT_TEXT_LENGTH = 150;
  
  public NewsList(Context context, List<NewsItem> objects) {
    super(context, R.layout.news_list_item, objects);
    sort(new NewsItemComparator());
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View row = convertView;
    if (row == null) {
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      row = inflater.inflate(R.layout.news_list_item, parent, false);
    }

    NewsItem item = getItem(position);

    TextView title = (TextView) row.findViewById(R.id.news_item_title);
    title.setText(Html.fromHtml(item.title));

    // TODO: add date as well
    TextView association = (TextView) row.findViewById(R.id.news_item_association);
    association.setText(Html.fromHtml(item.club));

    // TODO: can we cut off this text in a more intelligent way?
    String shorty = intelligentCutOff(item.description);

    TextView shorttxt = (TextView) row.findViewById(R.id.news_item_short);
    shorttxt.setText(Html.fromHtml(shorty));
    return row;
  }
 
  private String intelligentCutOff(String description) {
	  String shorty;
	  
	  if(description.length() < SHORT_TEXT_LENGTH) {
		  return description;
	  } 
	  
	  shorty = description.substring(0, SHORT_TEXT_LENGTH);
	  
	  while(shorty.charAt(shorty.length()-1) != ' ') {
		  shorty = shorty.substring(0, shorty.length() - 1);
	  }
	  
	  shorty += "...";
	  return shorty;
  }
  
  private class NewsItemComparator implements Comparator<NewsItem> {

    public int compare(NewsItem item1, NewsItem item2) {
      return item1.date.compareTo(item2.date);
    }
  }
 
}

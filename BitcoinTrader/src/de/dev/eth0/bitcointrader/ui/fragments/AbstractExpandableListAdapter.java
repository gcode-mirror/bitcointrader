//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Alexander Muthmann
 */
public abstract class AbstractExpandableListAdapter<T, U> extends BaseExpandableListAdapter {

  protected final AbstractBitcoinTraderActivity activity;
  protected final BitcoinTraderApplication application;
  protected final LayoutInflater inflater;
  private final List<T> listDataHeader = new ArrayList<T>();
  private final Map<T, List<U>> listData = new HashMap<T, List<U>>();
  private boolean showEmptyText = false;

  public AbstractExpandableListAdapter(AbstractBitcoinTraderActivity activity) {
    this.activity = activity;
    this.application = activity.getBitcoinTraderApplication();
    inflater = LayoutInflater.from(activity);
  }

  public void clear() {
    listDataHeader.clear();
    listData.clear();
    notifyDataSetChanged();
  }

  public void replace(Map<T, List<U>> orders) {
    this.listDataHeader.clear();
    this.listDataHeader.addAll(orders.keySet());
    this.listData.clear();
    this.listData.putAll(orders);
    showEmptyText = true;
    notifyDataSetChanged();
  }

  @Override
  public boolean isEmpty() {
    return showEmptyText && super.isEmpty();
  }

  @Override
  public int getChildrenCount(int groupPosition) {
    return listData.get(listDataHeader.get(groupPosition)).size();
  }

  @Override
  public int getGroupCount() {
    return listDataHeader.size();
  }

  @Override
  public U getChild(int groupPosition, int childPosititon) {
    return listData.get(listDataHeader.get(groupPosition)).get(childPosititon);
  }

  @Override
  public long getChildId(int groupPosition, int childPosition) {
    return getChild(groupPosition, childPosition).hashCode();
  }

  @Override
  public T getGroup(int position) {
    return listDataHeader.get(position);
  }

  @Override
  public long getGroupId(int position) {
    return listDataHeader.get(position).hashCode();
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public boolean isChildSelectable(int groupPosition, int childPosition) {
    return true;
  }

  @Override
  public View getGroupView(int groupPosition, boolean isExpanded, View groupView, ViewGroup parent) {
    if (groupView == null) {
      groupView = inflater.inflate(getGroupLayout(), null);
    }
    T tx = getGroup(groupPosition);
    bindGroupView(groupView, tx);
    return groupView;
  }

  @Override
  public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View childView, ViewGroup parent) {
    if (childView == null) {
      childView = inflater.inflate(getChildLayout(), null);
    }
    U tx = getChild(groupPosition, childPosition);
    bindChildView(childView, tx);
    return childView;
  }

  public abstract int getGroupLayout();

  public abstract int getChildLayout();

  public abstract void bindGroupView(View group, final T entry);

  public abstract void bindChildView(View child, final U entry);

}

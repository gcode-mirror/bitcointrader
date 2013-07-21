//$URL$
//$Id$
package de.dev.eth0.bitcointrader.ui.fragments;

import java.util.Collection;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import de.dev.eth0.bitcointrader.BitcoinTraderApplication;
import de.dev.eth0.bitcointrader.ui.AbstractBitcoinTraderActivity;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Muthmann
 */
public abstract class AbstractListAdapter<T> extends BaseAdapter {

  protected final AbstractBitcoinTraderActivity activity;
  protected final BitcoinTraderApplication application;
  private final LayoutInflater inflater;
  private final List<T> entries = new ArrayList<T>();
  private boolean showEmptyText = false;

  public AbstractListAdapter(AbstractBitcoinTraderActivity activity) {
    this.activity = activity;
    this.application = activity.getBitcoinTraderApplication();
    inflater = LayoutInflater.from(activity);
  }

  public void clear() {
    entries.clear();
    notifyDataSetChanged();
  }

  public void replace(Collection<T> orders) {
    this.entries.clear();
    this.entries.addAll(orders);
    showEmptyText = true;
    notifyDataSetChanged();
  }

  @Override
  public boolean isEmpty() {
    return showEmptyText && super.isEmpty();
  }

  @Override
  public int getCount() {
    return entries.size();
  }

  @Override
  public T getItem(int position) {
    return entries.get(position);
  }

  @Override
  public long getItemId(int position) {
    return entries.get(position).hashCode();
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  @Override
  public View getView(int position, View row, ViewGroup parent) {
    if (row == null) {
      row = inflater.inflate(getRowLayout(), null);
    }
    T tx = getItem(position);
    bindView(row, tx);
    return row;
  }

  public abstract int getRowLayout();

  public abstract void bindView(View row, final T entry);
}

# Sticky-Header-RecyclerView

This library allows you to create lists with sticky headers in a vertical or horizontal orientation.Supports **API 10** and above.

![Example vertical](img_example_vertical.gif)

## Usage

Create adapter and implement SHRVItemType interface.In getItemViewType function when you want to place header return TYPE_HEADER.

Adapter example:

    public class AdapterMain extends RecyclerView.Adapter<AdapterMain.ViewHolderMain> implements SHRVItemType {

    private final static int TYPE_ITEM = 1;

    ...

    @Override
    public int getItemViewType(int position) {
        if (/*Your if clause*/) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    ...

    }

Then instead standart layout manager use SHRVLinearLayoutManager:

    ...

    RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    mRecyclerView.setLayoutManager(new SHRVLinearLayoutManager(SHRVLinearLayoutManager.VERTICAL));
    mRecyclerView.setAdapter(new AdapterMain(this));
    
    ...
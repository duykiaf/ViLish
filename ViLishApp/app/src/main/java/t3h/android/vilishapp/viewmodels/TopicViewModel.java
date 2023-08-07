package t3h.android.vilishapp.viewmodels;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.rxjava3.PagingRx;

import io.reactivex.rxjava3.core.Flowable;
import kotlinx.coroutines.CoroutineScope;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.models.Topic;
import t3h.android.vilishapp.paging.TopicPagingSource;

public class TopicViewModel extends ViewModel {
    Pager<Integer, Topic> pager = new Pager<>(
            new PagingConfig(AppConstant.TOPICS_LIST_PAGE_SIZE),
            TopicPagingSource::new
    );

    public Flowable<PagingData<Topic>> flowable;

    public TopicViewModel() {
        CoroutineScope viewModelScope = ViewModelKt.getViewModelScope(this);
        flowable = PagingRx.getFlowable(pager);
        {
            PagingRx.cachedIn(flowable, viewModelScope);
        }
    }
}

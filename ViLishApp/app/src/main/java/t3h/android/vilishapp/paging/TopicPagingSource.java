package t3h.android.vilishapp.paging;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagingState;
import androidx.paging.rxjava3.RxPagingSource;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.models.Topic;
import t3h.android.vilishapp.models.TopicResults;
import t3h.android.vilishapp.repositories.TopicRepository;

public class TopicPagingSource extends RxPagingSource<Integer, Topic> {
    private TopicRepository topicRepository;

    public TopicPagingSource() {
        topicRepository = new TopicRepository();
    }

    @Nullable
    @Override
    public Integer getRefreshKey(@NonNull PagingState<Integer, Topic> pagingState) {
        Integer anchorPosition = pagingState.getAnchorPosition();
        if (anchorPosition == null) {
            return null;
        }

        LoadResult.Page<Integer, Topic> anchorPage = pagingState.closestPageToPosition(anchorPosition);
        if (anchorPage == null) {
            return null;
        }

        Integer prevKey = anchorPage.getPrevKey();
        if (prevKey != null) {
            return prevKey + 1;
        }

        Integer nextKey = anchorPage.getNextKey();
        if (nextKey != null) {
            return nextKey - 1;
        }

        return null;
    }

    @NonNull
    @Override
    public Single<LoadResult<Integer, Topic>> loadSingle(@NonNull LoadParams<Integer> loadParams) {
        // Start refresh at page 1 if undefined.
        Integer nextPageNumber = loadParams.getKey();
        if (nextPageNumber == null) {
            nextPageNumber = 1;
        }

        Integer finalNextPageNumber = nextPageNumber;
        return topicRepository.getTopicsListByPage(false, nextPageNumber, null)
                .subscribeOn(Schedulers.io())
                .map(topicResults -> toLoadResult(topicResults, finalNextPageNumber))
                .onErrorReturn(LoadResult.Error::new);
    }

    private LoadResult<Integer, Topic> toLoadResult(
            @NonNull TopicResults response, Integer currentPage) {
        return new LoadResult.Page<>(
                response.getTopicList(),
                null, // Only paging forward.
                response.getTopicList().size() > 0 ? currentPage + 1 : null,
                LoadResult.Page.COUNT_UNDEFINED,
                LoadResult.Page.COUNT_UNDEFINED);
    }
}

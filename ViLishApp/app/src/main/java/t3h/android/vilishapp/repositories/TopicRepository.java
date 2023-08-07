package t3h.android.vilishapp.repositories;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.schedulers.Schedulers;
import t3h.android.vilishapp.helpers.AppConstant;
import t3h.android.vilishapp.models.Topic;
import t3h.android.vilishapp.models.TopicResults;

public class TopicRepository {
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    public TopicRepository() {
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        databaseReference = database.getReference().child(AppConstant.TOPICS);
        databaseReference.keepSynced(true);
    }

    public Single<TopicResults> getTopicsListByPage(boolean isSearching, int nextPageNumber, String searchKeyword) {
        int startAt = (nextPageNumber - 1) * AppConstant.TOPICS_LIST_PAGE_SIZE;
        Query query = databaseReference.orderByChild(AppConstant.STATUS).equalTo(AppConstant.ACTIVE)
                .startAt(startAt).limitToFirst(AppConstant.TOPICS_LIST_PAGE_SIZE);

        return Single.create((SingleEmitter<TopicResults> emitter) ->
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<Topic> topicList = new ArrayList<>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            Topic topic = data.getValue(Topic.class);
                            if (topic != null) {
                                topicList.add(topic);
                            }
                        }
                        emitter.onSuccess(new TopicResults(topicList));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        emitter.onError(error.toException());
                    }
                })).subscribeOn(Schedulers.io());
    }
}

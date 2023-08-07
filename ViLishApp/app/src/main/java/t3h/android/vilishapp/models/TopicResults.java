package t3h.android.vilishapp.models;

import java.util.List;

public class TopicResults {
    private List<Topic> topicList;

    public TopicResults() {
    }

    public TopicResults(List<Topic> topicList) {
        this.topicList = topicList;
    }

    public List<Topic> getTopicList() {
        return topicList;
    }

    public void setTopicList(List<Topic> topicList) {
        this.topicList = topicList;
    }
}

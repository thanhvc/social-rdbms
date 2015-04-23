package org.exoplatform.social.core.entity;

import java.util.Date;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.Table;

/**
 * Created by bdechateauvieux on 3/24/15.
 */
@Entity
@Table(name = "SOC_COMMENTS")
@EntityListeners({Comment.CommentEntityListener.class})
@NamedQuery(
  name = "getActivityByComment",
  query = "select a from Activity a join a.comments Comment where Comment.id = :COMMENT_ID"
)
public class Comment extends BaseActivity{
  @Id
  @GeneratedValue
  @Column(name="COMMENT_ID")
  private Long id;

//  @ElementCollection
//  @CollectionTable(
//    name = "SOC_COMMENT_LIKERS",
//    joinColumns=@JoinColumn(name = "COMMENT_ID")
//  )
//  @Column(name="LIKER_ID")
//  private List<String> likerIds;

  @ElementCollection
  @JoinTable(
    name = "SOC_COMMENT_TEMPLATE_PARAMS",
    joinColumns=@JoinColumn(name = "COMMENT_ID")
  )
  @MapKeyColumn(name="TEMPLATE_PARAM_KEY")
  @Column(name="TEMPLATE_PARAM_VALUE")
  private Map<String, String> templateParams;

  public Comment() {
    setPosted(new Date());
    setLastUpdated(new Date());
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  @Override
  public String toString() {
    return "Comment[id = " + id + ",owner = " + getOwnerId() + ",title = " + getTitle() + "]";
  }

  public static class CommentEntityListener {
    @PostPersist
    public void onPostPersist(Comment comment) {
    }
  }
}

/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.addons.storage.entity;

import java.io.Serializable;
import java.util.*;

import javax.persistence.*;

import org.json.JSONObject;

import org.exoplatform.commons.api.persistence.ExoEntity;

/**
 * Created by bdechateauvieux on 3/24/15.
 */
@Entity(name = "SocActivity")
@ExoEntity
@Table(name = "SOC_ACTIVITIES")
@NamedQueries({
        @NamedQuery(
                name = "getActivityByComment",
                query = "select a from SocActivity a join a.comments Comment where Comment.id = :COMMENT_ID"
        ),
        @NamedQuery(name = "SocActivity.migratePosterId", query = "UPDATE SocActivity a SET a.posterId = :newId WHERE a.posterId = :oldId"),
        @NamedQuery(name = "SocActivity.migrateOwnerId", query = "UPDATE SocActivity a SET a.ownerId = :newId WHERE a.ownerId = :oldId"),

        @NamedQuery(name = "SocActivity.getAllActivities", query = "SELECT a FROM SocActivity a WHERE a.isComment = false AND a.parent IS NULL"),
        @NamedQuery(name = "SocActivity.findCommentsOfActivity", query = "SELECT a FROM SocActivity a WHERE a.parent.id = :activityId ORDER BY a.updatedDate ASC"),
        @NamedQuery(name = "SocActivity.numberCommentsOfActivity", query = "SELECT count(distinct a) FROM SocActivity a WHERE a.parent.id = :activityId"),
        @NamedQuery(name = "SocActivity.findNewerCommentsOfActivity",
                query = "SELECT a FROM SocActivity a WHERE a.parent.id = :activityId AND a.updatedDate > :sinceTime ORDER BY a.updatedDate ASC"),
        @NamedQuery(name = "SocActivity.findOlderCommentsOfActivity",
                query = "SELECT a FROM SocActivity a WHERE a.parent.id = :activityId AND a.updatedDate < :sinceTime ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getParentActivity",
                query = "SELECT a FROM SocActivity a INNER JOIN a.comments c WHERE c.id = :commentId"),
        @NamedQuery(name = "SocActivity.getActivityByOwner",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND exists (SELECT item FROM a.streamItems item WHERE item.ownerId in (:owner)) ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getOlderActivityByOwner",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.updatedDate < :sinceTime AND exists (SELECT item FROM a.streamItems item WHERE item.ownerId in (:owner)) ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getNewerActivityByOwner",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false  AND a.updatedDate > :sinceTime AND exists (SELECT item FROM a.streamItems item WHERE item.ownerId in (:owner)) ORDER BY a.updatedDate ASC"),
        @NamedQuery(name = "SocActivity.getActivityByOwnerAndProviderId",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.providerId = :providerId AND exists (SELECT item FROM a.streamItems item WHERE item.ownerId in (:owner)) ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getActivityFeed",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE item.ownerId in (:owners) " +
                        "         OR exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getNewerActivityFeed",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.updatedDate > :sinceTime AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE item.ownerId in (:owners) " +
                        "         OR exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate ASC"),
        @NamedQuery(name = "SocActivity.getOlderActivityFeed",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.updatedDate < :sinceTime AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE item.ownerId in (:owners) " +
                        "         OR exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getActivityOfConnection",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE " +
                        "         exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate DESC"),
        @NamedQuery(name = "SocActivity.getNewerActivityOfConnection",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.updatedDate > :sinceTime AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE " +
                        "         exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate ASC"),
        @NamedQuery(name = "SocActivity.getOlderActivityOfConnection",
                query = "SELECT a FROM SocActivity a WHERE a.hidden = false AND a.updatedDate < :sinceTime AND exists (" +
                        "   SELECT item FROM a.streamItems item WHERE " +
                        "         exists (SELECT c.sender.id FROM SocConnection c WHERE c.receiver.id = :ownerid AND c.sender.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        "         OR exists (SELECT c.receiver.id FROM SocConnection c WHERE c.sender.id = :ownerid AND c.receiver.id = item.ownerId AND c.status = :connStatus AND item.streamType = :connStreamType)" +
                        ") ORDER BY a.updatedDate DESC")
})
public class ActivityEntity implements Serializable {

  private static final long serialVersionUID = -1489894321243127979L;

  @Id
  @SequenceGenerator(name="SEQ_SOC_ACTIVITIES_ID", sequenceName="SEQ_SOC_ACTIVITIES_ID")
  @GeneratedValue(strategy=GenerationType.AUTO, generator="SEQ_SOC_ACTIVITIES_ID")
  @Column(name="ACTIVITY_ID")
  private Long id;

  /** */
  @Column(name="TITLE", nullable = false)
  private String title;

  /** */
  @Column(name="TYPE")
  private String type;

  /** */
  @Column(name="TITLE_ID")
  private String titleId;

  /** */
  @Column(name="POSTED", nullable = false)
  protected Long posted;

  /** */
  @Column(name="UPDATED_DATE", nullable = false)
  private Long updatedDate;

  /** */
  @Column(name="POSTER_ID")
  private String posterId;// creator

  /** */
  @Column(name="OWNER_ID")
  private String ownerId;// owner of stream

  /** */
  @Column(name="PERMALINK")
  private String permaLink;

  /** */
  @Column(name="APP_ID")
  private String appId;

  /** */
  @Column(name="EXTERNAL_ID")
  private String externalId;

  /** */
  @Column(name="LOCKED", nullable = false)
  private Boolean locked = false;

  /** */
  @Column(name="HIDDEN", nullable = false)
  private Boolean hidden = false;

  @Column(name="BODY", length = 2000)
  private String body;
  
  @ElementCollection
  @CollectionTable(
    name = "SOC_ACTIVITY_LIKERS",
    joinColumns=@JoinColumn(name = "ACTIVITY_ID")
  )
  private Set<LikerEntity> likers = new HashSet<LikerEntity>();

  @ElementCollection
  @JoinTable(
    name = "SOC_ACTIVITY_TEMPLATE_PARAMS",
    joinColumns=@JoinColumn(name = "ACTIVITY_ID")
  )
  @MapKeyColumn(name="TEMPLATE_PARAM_KEY")
  @Column(name="TEMPLATE_PARAM_VALUE")
  private Map<String, String> templateParams = new LinkedHashMap<String, String>();

  @Column(name = "IS_COMMENT")
  private boolean isComment = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "PARENT_ID", nullable = true)
  private ActivityEntity parent;

  @OneToMany(cascade = {CascadeType.REMOVE}, orphanRemoval=true, mappedBy="parent", fetch=FetchType.LAZY)
  private List<ActivityEntity> comments;

  @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="activity", fetch=FetchType.LAZY)
  private Set<MentionEntity> mentions;

  /** */
  @Column(name="PROVIDER_ID")
  private String providerId;
  
  /** */
  @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="activity", fetch=FetchType.LAZY)
  private List<StreamItemEntity> streamItems;

  /** */
  public ActivityEntity() {
    setPosted(new Date());
    setUpdatedDate(new Date());
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public String getTitleId() {
    return titleId;
  }
  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }
  public Date getPosted() {
    return (posted != null && posted > 0) ? new Date(posted) : null;
  }
  public void setPosted(Date posted) {
    this.posted = (posted != null ? posted.getTime() : 0);
  }
  public Date getUpdatedDate() {
    return updatedDate != null && updatedDate > 0 ? new Date(updatedDate) : null;
  }
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = (updatedDate != null ? updatedDate.getTime() : 0);
  }
  public String getPosterId() {
    return posterId;
  }
  public void setPosterId(String posterId) {
    this.posterId = posterId;
  }
  public String getOwnerId() {
    return ownerId;
  }
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }
  public String getPermaLink() {
    return permaLink;
  }
  public void setPermaLink(String permaLink) {
    this.permaLink = permaLink;
  }
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }
  public String getExternalId() {
    return externalId;
  }
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }
  public Boolean getLocked() {
    return locked;
  }
  public void setLocked(Boolean locked) {
    this.locked = locked;
  }
  public Boolean getHidden() {
    return hidden;
  }
  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }
  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }

  public void addLiker(String likerId) {
    LikerEntity liker = new LikerEntity(likerId);
    if (!this.likers.contains(liker)) {
      this.likers.add(liker);
    }
  }

  public Set<LikerEntity> getLikers() {
    return likers;
  }

  public Set<String> getLikerIds() {
    Set<String> ids = new HashSet<String>();
    for (LikerEntity liker : likers) {
      ids.add(liker.getLikerId());      
    }
    return ids;
  }

  public void setLikerIds(Set<String> likerIds) {
    if (likerIds == null || likerIds.isEmpty()) {
      this.likers.clear();
    } else {
      //clean
      Iterator<LikerEntity> itor = likers.iterator();
      while (itor.hasNext()) {
        LikerEntity liker = itor.next();
        if (!likerIds.contains(liker.getLikerId())) {
          itor.remove();
        }
      }
      //add new
      for (String id : likerIds) {        
        addLiker(id);
      }
    }
  }

  public Set<String> getMentionerIds() {
    Set<String> result = new HashSet<String>();
    if (this.mentions!=null) {
      for (MentionEntity mention : this.mentions) {
        result.add(mention.getMentionId());
      }
    }
    return result;
  }

  public void setMentionerIds(Set<String> mentionerIds) {
    if (this.mentions==null) {
      this.mentions = new HashSet<>();
    }

    Set<String> mentionToAdd = new HashSet<>(mentionerIds);
    Set<MentionEntity> mentioned = new HashSet<>(this.mentions);
    for (MentionEntity m : mentioned) {
      if (!mentionerIds.contains(m.getMentionId())) {
        this.mentions.remove(m);
      } else {
        mentionToAdd.remove(m.getMentionId());
      }
    }

    for (String mentionerId : mentionToAdd) {
      addMention(mentionerId);
    }
  }

  private void addMention(String mentionerId) {
    if (this.mentions==null) {
      this.mentions = new HashSet<>();
    }
    MentionEntity mention = new MentionEntity();
    mention.setMentionId(mentionerId);
    mention.setActivity(this);
    this.mentions.add(mention);
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  public List<ActivityEntity> getComments() {
    return comments;
  }

  public void setComments(List<ActivityEntity> comments) {
    this.comments = comments;
  }

  /**
   * Adds the comment item entity to this activity
   * @param comment - the comment entity
   */
  public void addComment(ActivityEntity comment) {
    if (this.comments == null) {
      this.comments = new ArrayList<>();
    }
    comment.setParent(this);
    this.comments.add(comment);
  }

  public ActivityEntity getParent() {
    return parent;
  }

  public void setParent(ActivityEntity parent) {
    this.parent = parent;
  }

  public boolean isComment() {
    return isComment;
  }

  public void setComment(boolean comment) {
    isComment = comment;
  }

  public List<StreamItemEntity> getStreamItems() {
    return streamItems;
  }

  public void setStreamItems(List<StreamItemEntity> streamItems) {
    this.streamItems = streamItems;
  }

  /**
   * Adds the stream item entity to this activity
   * @param item the stream item
   */
  public void addStreamItem(StreamItemEntity item) {
    if (this.streamItems == null) {
      this.streamItems = new ArrayList<StreamItemEntity>();
    }
    item.setActivity(this);
    this.streamItems.add(item);
  }
  
  public void removeStreamItem(StreamItemEntity item) {
    for (StreamItemEntity it : this.getStreamItems()) {
      if (it.getOwnerId().equals(item.getOwnerId()) && it.getStreamType().equals(item.getStreamType())) {
        this.streamItems.remove(it);
        break;
      }
    }
  }

  public String getProviderId() {
    return providerId;
  }
  
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }
  
  @Override
  public String toString() {
    return new JSONObject(this).toString();
  }
}

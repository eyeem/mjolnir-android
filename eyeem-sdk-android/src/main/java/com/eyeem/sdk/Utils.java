package com.eyeem.sdk;

import android.app.Application;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import com.eyeem.chips.DefaultBubbles;
import com.eyeem.chips.Linkify;
import com.eyeem.chips.Regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

/**
 * Created by vishna on 14/11/13.
 */
public class Utils {

   public static String CDN_PATH = "cdn.eyeem.com";
   public static String PHOTO_PATH = "http://" + CDN_PATH + "/";
   public static String THUMB_BASE = "thumb/";

   private static Application app;
   public static int with;
   public static int verb_at;
   public static int verb_in;

   public static void init(Application app) {
      Utils.app = app;
   }

   public static void computeEntities(Photo p) {
      p.entities.clear();
      StringBuilder sb = new StringBuilder();
      // description
      if (!TextUtils.isEmpty(p.description)) {
         Matcher matcher = Regex.VALID_BUBBLE.matcher(p.description.trim());
         while (matcher.find()) {
            String id = matcher.group(1);
            Linkify.Entity entity = new Linkify.Entity(matcher.start(), matcher.end(), id, id, Linkify.Entity.ALBUM);
            if (p.albums != null) {
               for (Album a : p.albums.items) {
                  if (a.id.equals(id)) {
                     entity.text = a.name;
                     break;
                  }
               }
            }
            p.entities.add(entity);
         }
         sb.append(p.description.trim());
      }
      // links, emails & mentions
      p.entities.addAll(Linkify.computeEntities(p.description));
      if (app == null) {
         p.entityfiedDescription = sb.toString();
         return;
      }
      // people
      if (p.people != null && p.people.items != null && p.people.items.size() > 0) {

         String[] args = new String[p.people.items.size()];
         for (int i=0; i < p.people.items.size(); i++) {
            args[i] = p.people.items.get(i).toString();
         }
         if (sb.length() > 0)
            sb.append(" ");
         int offset = sb.length();
         int textOffset = 0;
         String text = with_people(args);
         for (int i = 0; i < args.length; i++) {
            String person = args[i];
            int personIndex = text.substring(textOffset, text.length()).indexOf(person);
            if (personIndex == -1)
               break;

            if ("eyeem".equals(p.people.items.get(i).serviceType)) {
               p.entities.add(new Linkify.Entity(
                  offset + textOffset + personIndex,
                  offset + textOffset + personIndex + person.length(),
                  p.people.items.get(i).serviceId,
                  p.people.items.get(i).toString(),
                  Linkify.Entity.PERSON)
               );
            }
            textOffset = personIndex + person.length();
         }
         sb.append(text);
      }
      // location
      Album venue = null;
      Album city = null;
      Album country = null;
      if (p.albums != null && p.albums.items != null) {
         for (Album album : p.albums.items) {
            if (Album.TYPE_VENUE.equals(album.type)) {
               venue = album;
            } else if (Album.TYPE_CITY.equals(album.type)) {
               city = album;
            } else if (Album.TYPE_COUNTRY.equals(album.type)) {
               country = album;
            }
         }
      }

      if (venue != null && !TextUtils.isEmpty(venue.name)) {
         if (sb.length() > 0)
            sb.append(" ");
         sb.append(getString(verb_at)).append(" ");
         p.entities.add(new Linkify.Entity(sb.length(), sb.length() + venue.name.length(), venue.id, venue.name, Linkify.Entity.VENUE));
         sb.append(venue.name);
      }
      if (city != null && !TextUtils.isEmpty(city.name)) {
         if (sb.length() > 0)
            sb.append(" ");
         if (venue == null || TextUtils.isEmpty(venue.name)) {
            sb.append(getString(verb_in)).append(" ");
         }
         p.entities.add(new Linkify.Entity(sb.length(), sb.length()+city.name.length(), city.id, city.name, Linkify.Entity.CITY));
         sb.append(city.name);
         if (country != null && !TextUtils.isEmpty(country.name)) {
            sb.append(",");
         }
      }
      if (country != null && !TextUtils.isEmpty(country.name)) {
         if (sb.length() > 0)
            sb.append(" ");
         if ((venue == null || TextUtils.isEmpty(venue.name)) && (city == null || TextUtils.isEmpty(city.name))) {
            sb.append(getString(verb_in)).append(" ");
         }
         p.entities.add(new Linkify.Entity(sb.length(), sb.length()+country.name.length(), country.id, country.name, Linkify.Entity.COUNTRY));
         sb.append(country.name);
      }

      p.entityfiedDescription = sb.toString();
   }

   public static String with_people(String... people) {
      int n = people.length;
      StringBuilder sb = new StringBuilder(getString(with));
      for (int i = 0; i<n; i++) {
         if (i == 0) {
            sb.append(" ");
         } else if (i + 1 == n) {
            sb.append(" & ");
         } else if (i > 0) {
            sb.append(", ");
         }
         sb.append(people[i]);
      }
      return sb.toString();
   }

   public static String getString(int id) {
      if (app == null)
         return "";
      return app.getString(id);
   }

   public static SpannableStringBuilder bubblify(Photo p, int textSize) {
      if (p.entityfiedDescription == null) {
         computeEntities(p);
      }
      SpannableStringBuilder ssb = new SpannableStringBuilder(p.entityfiedDescription);
//      int width = public_image_caption.getWidth()
//         - public_image_caption.getPaddingRight()
//         - public_image_caption.getPaddingLeft();
      int width = 0;
      if (p.entities == null)
         return ssb;
      for (Linkify.Entity entity : p.entities) {
         switch (entity.type) {
            case Linkify.Entity.ALBUM:
               com.eyeem.chips.Utils.bubblify(ssb, entity.text, entity.start, entity.end,
                  width, DefaultBubbles.get(0, app, textSize), null, entity);
               break;
            case Linkify.Entity.VENUE:
               com.eyeem.chips.Utils.bubblify(ssb, entity.text, entity.start, entity.end,
                  width, DefaultBubbles.get(4, app, textSize), null, entity);
               break;
            case Linkify.Entity.CITY:
            case Linkify.Entity.COUNTRY:
            case Linkify.Entity.PERSON:
            case Linkify.Entity.EMAIL:
            case Linkify.Entity.MENTION:
            case Linkify.Entity.URL:
               com.eyeem.chips.Utils.tapify(ssb, entity.start, entity.end, 0xff000000, 0xffffffff, entity);
               break;
            default:
               // NOOP
         }
      }
      return ssb;
   }

   public static String lastSegment(String thumbUrl) {
      try {
         return Uri.parse(thumbUrl).getLastPathSegment();
      } catch (Exception e) {
         return null;
      }
   }

   private final static int[] THUMB_HEIGHT_VALUES = {75, 180, 240, 375, 480, 600};

   public static int normalizeSize(int size) {
      if (size >= THUMB_HEIGHT_VALUES[THUMB_HEIGHT_VALUES.length - 1]) {
         size = THUMB_HEIGHT_VALUES[THUMB_HEIGHT_VALUES.length - 1];
      } else for (int i = 0; i < THUMB_HEIGHT_VALUES.length; i++) {
         if (size <= THUMB_HEIGHT_VALUES[i]) {
            size = THUMB_HEIGHT_VALUES[i];
            break;
         }
      }
      return size;
   }

   public static String getThumbnailPathByHeight(int height, String filename) {
      return PHOTO_PATH + THUMB_BASE + "h/" + normalizeSize(height) + "/" + filename;
   }

   public static String getThumbnailPathByWidth(int width, String filename) {
      return PHOTO_PATH + THUMB_BASE + "w/" + normalizeSize(width) + "/" + filename;
   }

   public static String getSquareThumbnail(int side, User user) {
      return getSquareThumbnail(side, user.thumbUrl, user.photofilename);
   }

   public static String getSquareThumbnail(int side, String thumbUrl, String photofilename) {
      if (TextUtils.isEmpty(thumbUrl)) {
         return "http://cdn.eyeem.com/thumb/sq/" + side + "/placeholder.jpg";
      }

      if (thumbUrl.startsWith("file://")) return thumbUrl;

      if (thumbUrl.startsWith("https://graph.facebook.com") || thumbUrl.startsWith("http://graph.facebook.com"))
         return thumbUrl + String.format(Locale.US, "&width=%d&height=%d", side, side);
      if (thumbUrl.contains("pbs.twimg.com/profile_images/")) {
         return thumbUrl.replace("_normal", "");
      }
      if (thumbUrl.contains("eyeem")) {
         if (photofilename == null) photofilename = Utils.lastSegment(thumbUrl);
         return PHOTO_PATH + THUMB_BASE + "sq/" + side + "/" + photofilename;
      }
      return thumbUrl;
   }

   /* package */ static String cardItemId(CardItem cardItem) {
      try {
         if (Card.TYPE_BLOG_POST.equals(cardItem.type)) {
            return String.valueOf(cardItem.blogPost.url);
         } else if (Card.TYPE_FEATURED_USER.equals(cardItem.type)) {
            return cardItem.user.id;
         } else if (Card.TYPE_FEATURED_ALBUM.equals(cardItem.type) || Card.TYPE_FEATURED_CITY.equals(cardItem.type)) {
            return cardItem.album.id;
         } else if (Card.TYPE_AROUND_YOU.equals(cardItem.type)) {
            return Card.TYPE_AROUND_YOU + cardItem.lat + cardItem.lng;
         } else if (Card.TYPE_POPULAR.equals(cardItem.type)) {
            StringBuilder sb = new StringBuilder(Card.TYPE_POPULAR);
            for (Photo photo : cardItem.photos) {
               sb.append(photo.id);
            }
            return sb.toString();
         }
      } catch (Exception e) {}

      // random id when other stuff fails
      return String.valueOf(java.util.UUID.randomUUID().getMostSignificantBits());
   }

   /* package */ static String feedItemId(FeedItem feedItem) {
      try {
         if (FeedItem.TYPE_PHOTO.equals(feedItem.type)) {
            return FeedItem.TYPE_PHOTO+feedItem.photo.id;
         } else if (FeedItem.TYPE_ALBUM.equals(feedItem.type)) {
            return FeedItem.TYPE_ALBUM+feedItem.album.id;
         } else if (FeedItem.TYPE_PHOTO_GROUP.equals(feedItem.type)) {
            return FeedItem.TYPE_PHOTO_GROUP+feedItem.photoGroup.updated;
         }
      } catch (Exception e) {}

      // random id when other stuff fails
      return String.valueOf(java.util.UUID.randomUUID().getMostSignificantBits());
   }

   /* package */ static void calculateBatchIds(Batch batch) {
      batch.id = String.valueOf(batch.generated);
      if (batch.cards == null || batch.cards.items == null) return;
      for (int i = 0; i < batch.cards.items.size(); i++) {
         Card card = batch.cards.items.get(i);
         if (card == null) continue;
         card.id = batch.id + "_" + i;
      }
   }

   /**
    * Return ids of photo items that should be removed
    * @param list (should be frozen)
    * @return
    */
   public static ArrayList<String> dedupePhotoGroups(List list) {
      ArrayList<String> ids = new ArrayList<>();

      int n = list.size();
      for (int i = 0; i < n - 1; i++) {
         FeedItem item = validPhotoGroupFeedItem(list.get(i));
         if (item == null) continue;
         if (item.photoGroup.photoIds.size() == 0) {
            ids.add(item.id);
            continue;
         }

         // now check other groups in the list and mark them for removal if they overlap
         for (int j = i + 1; j < n; j++) {
            FeedItem otherItem = validPhotoGroupFeedItem(list.get(j));
            if (otherItem == null || ids.contains(otherItem.id)) continue;
            if (otherItem.photoGroup.photoIds.size() == 0) {
               ids.add(otherItem.id);
               continue;
            }
            if (!FeedItem.TYPE_PHOTO_GROUP.equals(otherItem.type)) continue;

            // photo groups must be of the same user
            if (!item.photoGroup.userId.equals(otherItem.photoGroup.userId)) continue;

            ArrayList<String> IDs = new ArrayList<>(item.photoGroup.photoIds);
            ArrayList<String> otherIDs = new ArrayList<>(otherItem.photoGroup.photoIds);

            IDs.retainAll(otherIDs);

            if (IDs.size() > 0) { // Intersection between groups exists. Remove.
               ids.add(otherItem.id);
            }
         }
      }

      return ids;
   }

   public static FeedItem validPhotoGroupFeedItem(Object o) {
      if (!(o instanceof FeedItem)) return null;
      FeedItem item = (FeedItem) o;
      if (!FeedItem.TYPE_PHOTO_GROUP.equals(item.type)) return null;
      if (item.photoGroup != null && item.photoGroup.photoIds != null && !TextUtils.isEmpty(item.photoGroup.userId)) return item;
      return null;
   }
}

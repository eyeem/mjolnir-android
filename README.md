Android Mjolnir Library
=================

Core package that can easily bind with `mjolnir-ruby` generated classes enabling efortless REST API libraries development.

Dependencies
============

Mjolnir uses Google's Volley library for asynchronous API calls and OkHttp as the default HTTP client.

Usage
============
The best way to learn currently how to use this library is just to take a look on `EyeEm.java` in `eyeem-sdk-android` library:

Assuming you want to use Volley, you'll need a `RequestQueue`. You can and most likely you should define one that will rely on `OkHttp`.

``` java
RequestQueue queue = Volley.newRequestQueue(this, new OkHttpStack());
```

Let's say you want to obtain a list of photos, you do then something like this:

```java
// if you wish to obtain a list of photos, do something like:
new EyeEm("/v2/users/vishna/photos") // choose endpoint
  .jsonpath("photos.items")          // specify json path
  .param("includeLikes", "1")        // ...and some params
  .listOf(Photo.class)               // ...and type of response
  .listener(new Response.Listener<List>() {
    @Override                        // handle the response
    public void onResponse(List list) {
      for (Object photo : list) {
        Log.i("EyeEm SDK", ((Photo)photo).description);
      }
    }
})
.enqueue(queue);                     // ...and add to queue

```

If you need to perform an authorized call, you can invoke `OAuthFragment` that will resolve stuff for you. It handles rotation gracefully.

```java
OAuthFragment.show(
  getActivity().getSupportFragmentManager(), // fragment manager
  queue,                                     // request queue
  new EyeEm.Account()                        // instance of an account
);
```

Accounts are stored in a `JSON` serialiazed form in `SharedPreferences`. You can access them in the following manner:

```java
HashSet<Account> accounts = Account.getByType(context, "eyeem");
```

Now to authorize your calls you simply need to add `.with(account)` to the chain of invocations, e.g.

```java
new EyeEm("/v2/users/me")
  .jsonpath("user")
  .with(account)
  .objectOf(User.class)
  .listener(new Response.Listener<Object>() {
    @Override public void onResponse(Object o) {
      user = (User) o;
      Log.i("EyeEm SDK", "Hello "+ user.fullname);
    }
  })
.enqueue(queue);
```

Unit testing
============
https://github.com/codepath/android_guides/wiki/Robolectric-Installation-for-Unit-Testing

Developed By
============

* Lukasz Wisniewski

License
=======

    Copyright 2013 EyeEm Mobile GmbH

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
package contract.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class GsonContructor {

    private static final GsonBuilder gsonBuilder = new GsonBuilder();

    /**
     * Returns a Gson instance with all the required bells and whistles attached.
     *
     * @return A Gson instance that works properly.
     */
    public static Gson build () {
        return getGson(false);
    }

    /**
     * Returns a Gson instance with all the required bells and whistles attached.
     *
     * @param prettyPrinting
     *            Set to True to enable human-readable output.
     * @return A Gson instance that works properly.
     */
    public static Gson getGson (boolean prettyPrinting) {
        // gsonBuilder.registerTypeAdapter(AnnotatedVariable.class, new
        // AnnotatedVariableAdapter());
        if (prettyPrinting) {
            gsonBuilder.setPrettyPrinting();
        }
        return gsonBuilder.create();
    }

    // private static class AnnotatedVariableAdapter extends
    // TypeAdapter<AnnotatedVariable> {
    //
    // @Override
    // public void write(JsonWriter out, AnnotatedVariable value) throws
    // IOException {
    // out.beginObject();
    // out.endObject();
    // }
    //
    // @Override
    // public AnnotatedVariable read(JsonReader in) throws IOException {
    // String identifier = null, rawType = null, abstractType = null, visual =
    // null;
    // Map<String, Object> attributes = null;
    // in.beginObject();
    // while (in.hasNext()) {
    // String name = in.nextName();
    // switch (name) {
    // case "identifier":
    // identifier = in.nextString();
    // break;
    // case "rawType":
    // rawType = in.nextString();
    // break;
    // case "abstractType":
    // abstractType = in.nextString();
    // break;
    // case "visual":
    // visual = in.nextString();
    // break;
    // case "attributes":
    // attributes = new HashMap<String, Object>();
    // populateAttributes(attributes, in);
    // break;
    // }
    // }
    // in.endObject();
    // AnnotatedVariable av = new AnnotatedVariable(identifier,
    // RawType.fromString(rawType),
    // AbstractType.fromString(abstractType), VisualType.fromString(visual),
    // attributes);
    // return av;
    // }
    //
    // private void populateAttributes(Map<String, Object> attributes,
    // JsonReader in) throws IOException {
    // ArrayList<Integer> size = new ArrayList<Integer>();
    // in.beginObject();
    // if (in.peek() == JsonToken.END_OBJECT) {
    // return;
    // }
    // in.nextName(); // Assume nextName() is "size".
    // in.beginArray();
    // outer: while (true) {
    // if (in.peek() == JsonToken.NUMBER) {
    // size.add(in.nextInt());
    // } else {
    // break outer;
    // }
    // }
    // in.endArray();
    // in.endObject();
    // attributes.put(Key.size.name(), (Object) size);
    // }
    // }
}

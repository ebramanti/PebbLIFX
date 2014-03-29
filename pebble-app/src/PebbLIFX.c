#include "pebble.h"

#define NUM_MENU_SECTIONS 2

//  Defined keys used in communication with companion Android app.
#define BULB_DISCOVERY_REQUEST_KEY 0
#define APP_CLOSE_KEY 1
#define ON_OFF_REQUEST_KEY 2
#define BRIGHTNESS_REQUEST_KEY 3
#define COLOR_REQUEST_KEY 4

struct Bulb {
    char* label;
    uint8_t state;
    uint16_t brightness;
    uint16_t color;
};

typedef struct Bulb Bulb;

Bulb *bulbList;

// Required for loading screen
static BitmapLayer *bulb_graphics_layer;
static GBitmap *bulb_bitmap;
static TextLayer *loading_screen_text;
static char *loading_msg = "Loading Bulb Info...";

// Required for no network screen
static TextLayer *layer_sad_text;
static char *sad_face = ":(";
// static TextLayer layer_no_net_text;
// static char *no_net_msg = "No bulbs found.";

// Window for main menu
static Window *window;
static SimpleMenuLayer *simple_menu_layer;

// A simple menu layer can have multiple sections
static SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];

int numberOfBulbs;

static SimpleMenuItem all_bulbs[1];
static SimpleMenuItem* bulb_menu;

//  Pebble app queries phone to start discoverer on phone.
static void bulb_discovery_init (void) {
    DictionaryIterator *iter;
    if (app_message_outbox_begin(&iter) != APP_MSG_OK) {
        return;
    }
    if (dict_write_uint8(iter, 0, BULB_DISCOVERY_REQUEST_KEY) != DICT_OK) {
        return;
    }
    app_message_outbox_send();
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent Discovery Initialization");
}

static void bulb_change_state (int index) {
    DictionaryIterator *iter;
    if (app_message_outbox_begin(&iter) != APP_MSG_OK) {
        return;
    }
    if (dict_write_uint8(iter, 0, ON_OFF_REQUEST_KEY) != DICT_OK) {
        return;
    }
    if (dict_write_uint8(iter, 1, index + 1) != DICT_OK) {
        return;
    }
    if (dict_write_uint8(iter, 2, (bulbList[index].state == 0) ? 1 : 0) != DICT_OK) {
        return;
    }
    app_message_outbox_send();
    bulbList[index].state = (bulbList[index].state == 0) ? 1 : 0;
    //Update subtitle here.
    bulb_menu[index].subtitle = (bulbList[index].state == 0) ? "Off" : "On";
    layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer));
    //  Log will tell if discovery request was sent.
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent On/Off Command");
}

// You can capture when the user selects a menu icon with a menu item select callback.
static void menu_select_callback (int index, void *ctx) {
    bulb_change_state(index);
}

static void all_lights_on_off_callback (int index, void *ctx) {
    int countOfOn = 0;
    for (int i = 0; i < numberOfBulbs; i++) {
        countOfOn += bulbList[i].state;
    }
    DictionaryIterator *iter;
    if (app_message_outbox_begin(&iter) != APP_MSG_OK) {
        return;
    }
    if (dict_write_uint8(iter, 0, ON_OFF_REQUEST_KEY) != DICT_OK) {
        return;
    }
    if (dict_write_uint8(iter, 1, 0) != DICT_OK) {
        return;
    }
    if (dict_write_uint8(iter, 2, (countOfOn > 0) ? 0 : 1) != DICT_OK) {
        return;
    }
    app_message_outbox_send();
    for (int i = 0; i < numberOfBulbs; i++) {
        bulbList[i].state = (countOfOn > 0) ? 0 : 1;
        bulb_menu[i].subtitle = (countOfOn > 0) ? "Off" : "On";
    }
    layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer));
    //  Log will tell if discovery request was sent.
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent On/Off Command");
}

static void destroy_load_message() {
    text_layer_destroy(loading_screen_text);
    bitmap_layer_destroy(bulb_graphics_layer);
    gbitmap_destroy(bulb_bitmap);
}

//  Pebble app receives dictionary containing bulb info, etc.
static void process_bulb_network_data (DictionaryIterator *iter) {

    APP_LOG(APP_LOG_LEVEL_DEBUG, "Initiate processing of bulb network");

    numberOfBulbs = dict_find(iter, 1)->value->uint8;

    bulbList = malloc(sizeof(Bulb)*numberOfBulbs);

    int j = 2;
    for (int i = 0; i < numberOfBulbs; i++) {
        bulbList[i] = (Bulb) {
            .label = (char*)dict_find(iter, j++)->value,
            .state = dict_find(iter, j++)->value->data[0],
            .brightness = dict_find(iter, j++)->value->uint16,
            .color = dict_find(iter, j++)->value->uint16,
        };
    }

    bulb_menu = malloc(sizeof(SimpleMenuItem) * numberOfBulbs);

    all_bulbs[0] = (SimpleMenuItem){
        // You should give each menu item a title and callback
        .title = "All Lights",
        .callback = all_lights_on_off_callback,
    };

    //  Create sections for the bulb names.
    for (int i = 0; i < numberOfBulbs; i++) {
        bulb_menu[i] = (SimpleMenuItem){
            .title = bulbList[i].label,
            .subtitle = bulbList[i].state == 0 ? "Off" : "On",
            .callback = menu_select_callback,
        };
        APP_LOG(APP_LOG_LEVEL_INFO, "Added bulb to menu: %s", bulb_menu[i].title);
    }
    APP_LOG(APP_LOG_LEVEL_INFO, "Total bulbs found: %d", numberOfBulbs);
    menu_sections[0] = (SimpleMenuSection){
        .num_items = 1,
        .items = all_bulbs,
    };
    menu_sections[1] = (SimpleMenuSection){
        // Menu sections can also have titles as well
        .title = "Bulbs",
        .num_items = numberOfBulbs,
        .items = bulb_menu,
    };

    // Now we prepare to initialize the simple menu layer
    // We need the bounds to specify the simple menu layer's viewport size
    // In this case, it'll be the same as the window's
    Layer *window_layer = window_get_root_layer(window);
    GRect bounds = layer_get_frame(window_layer);

    // Initialize the simple menu layer
    simple_menu_layer = simple_menu_layer_create(bounds, window, menu_sections, NUM_MENU_SECTIONS, NULL);

    // Here is where we will kill the loading screen.
    destroy_load_message();

    // Add it to the window for display
    layer_add_child(window_layer, simple_menu_layer_get_layer(simple_menu_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Added menu layer!");
}

static void display_no_network_found () {
    layer_sad_text = text_layer_create(GRect(0,2,144,40));
    text_layer_set_text_alignment(layer_sad_text, GTextAlignmentCenter); // Center the text.
    text_layer_set_font(layer_sad_text, fonts_get_system_font(FONT_KEY_BITHAM_34_LIGHT_SUBSET));
    text_layer_set_text(layer_sad_text, sad_face);
    text_layer_set_text_color(layer_sad_text, GColorBlack);
    text_layer_set_background_color(layer_sad_text, GColorClear);
    layer_add_child(window_get_root_layer(window), text_layer_get_layer(layer_sad_text));
    APP_LOG(APP_LOG_LEVEL_INFO, "Building network not found.");
}

static void build_load_screen () {
    bulb_graphics_layer = bitmap_layer_create(GRect(((144 - 60)/2), 26, 60, 120));
    bulb_bitmap = gbitmap_create_with_resource(RESOURCE_ID_LIFX_BULB_BW);
    bitmap_layer_set_bitmap(bulb_graphics_layer, bulb_bitmap);
    layer_add_child(window_get_root_layer(window), bitmap_layer_get_layer(bulb_graphics_layer));

    loading_screen_text = text_layer_create(GRect(0,2,144,40));
    text_layer_set_text_alignment(loading_screen_text, GTextAlignmentCenter); // Center the text.
    text_layer_set_font(loading_screen_text, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
    text_layer_set_text(loading_screen_text, loading_msg);
    text_layer_set_text_color(loading_screen_text, GColorBlack);
    text_layer_set_background_color(loading_screen_text, GColorClear);
    layer_add_child(window_get_root_layer(window), text_layer_get_layer(loading_screen_text));
    APP_LOG(APP_LOG_LEVEL_INFO, "Building Loading Window.");
}

// This initializes the menu upon window load
static void window_load (Window *window) { // 144 x 168 is pebble screen size
    build_load_screen();
}

static void send_close_signal() {
    DictionaryIterator *iter;
    if (app_message_outbox_begin(&iter) != APP_MSG_OK) {
        return;
    }
    if (dict_write_uint8(iter, 0, APP_CLOSE_KEY) != DICT_OK) {
        return;
    }
    app_message_outbox_send();
}

// Deinitialize resources on window unload that were initialized on window load.
static void window_unload (Window *window) {
    if (loading_screen_text) {
        text_layer_destroy(loading_screen_text);
    }

    if (bulb_graphics_layer) {
        bitmap_layer_destroy(bulb_graphics_layer);
    }

    if (bulb_bitmap) {
        gbitmap_destroy(bulb_bitmap);
    }

    if (simple_menu_layer) {
        simple_menu_layer_destroy(simple_menu_layer);
    }
}

// Handles all messages from phone
static void handle_receive (DictionaryIterator *iter, void *context) {
    int message_type = dict_read_first(iter)->value->uint8;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Received response from phone");
    switch(message_type) {
    case 0:
        APP_LOG(APP_LOG_LEVEL_DEBUG, "No Network Found - Key Code: %d", message_type);
        display_no_network_found(); //TODO implement
        break;
    case 1:
        process_bulb_network_data(iter);
        break;
    case 2:
        //process_bulb_data(iter);
        break;
    case 3:
        APP_LOG(APP_LOG_LEVEL_DEBUG, "Lost Connection - Key Code: %d", message_type);
        //display_connection_lost(); //TODO implement
        break;
    }
}

//  Initializes app message protocols.
static void app_message_init (void) {
    app_message_open(app_message_inbox_size_maximum(), dict_calc_buffer_size(3, 3));
    app_message_register_inbox_received(handle_receive);
}

void init () {
    //  Initialize app message inbox/outbox.
    app_message_init();

    //  Sends message to phone to initialize discovery
    bulb_discovery_init();

    // This should not occur until bulb information is pulled from the app.
    window = window_create();

    // Setup the window handlers
    window_set_window_handlers(window, (WindowHandlers) {
        .load = window_load,
        .unload = window_unload,
    });

    window_stack_push(window, true /* Animated */);
}

void deinit () {
    window_destroy(window);
    destroy_load_message();
    free(bulbList);
    simple_menu_layer_destroy(simple_menu_layer);

    send_close_signal();
}

int main(void) {
    init();
    app_event_loop();
    deinit();
}

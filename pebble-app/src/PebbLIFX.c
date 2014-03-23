#include "pebble.h"

#define NUM_MENU_SECTIONS 2

//  Defined keys used in communication with companion Android app.
#define BULB_DISCOVERY_REQUEST_KEY 0
#define ON_OFF_REQUEST_KEY 1
#define BRIGHTNESS_REQUEST_KEY 2
#define COLOR_REQUEST_KEY 3

struct Bulb {
    char* label;
    uint8_t state;
    uint16_t brightness;
    uint16_t color;
}

struct Bulb bulbList[];

//  Materials for loading screen.
static Window *loading_screen;
static TextLayer* loading_screen_text;

static char* msg = "Loading\nBulb Info...";

static Window *window;
static SimpleMenuLayer *simple_menu_layer;

// A simple menu layer can have multiple sections
static SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];

int numberOfBulbs; 

static SimpleMenuItem all_bulbs[1];
static SimpleMenuItem* bulb_menu;

void loading_screen_init (void) {
    loading_screen = window_create();
    loading_screen_text = text_layer_create(GRect(0,52,144,40));
    text_layer_set_text_alignment(loading_screen_text, GTextAlignmentCenter); // Center the text.
    text_layer_set_font(loading_screen_text, fonts_get_system_font(FONT_KEY_GOTHIC_18_BOLD));
    text_layer_set_text(loading_screen_text, msg);
    text_layer_set_text_color(loading_screen_text, GColorBlack);
    layer_add_child(window_get_root_layer(loading_screen), text_layer_get_layer(loading_screen_text));
    window_stack_push(loading_screen, true /* Animated */);
    APP_LOG(APP_LOG_LEVEL_INFO, "Building Loading Window.");
}

void loading_screen_destroy (void) {
    APP_LOG(APP_LOG_LEVEL_INFO, "Destroying Loading Window.");
    text_layer_destroy(loading_screen_text);
    window_destroy(loading_screen);
}

//  Pebble app queries phone to start discoverer on phone.
static void bulb_discovery_init (void) {
    DictionaryIterator *iter;
    if (app_message_outbox_begin(&iter) != APP_MSG_OK) {
        return;
    }
    //
    // The problem is here.
    //
    if (dict_write_uint8(iter, BULB_DISCOVERY_REQUEST_KEY, 0) != DICT_OK) {
        return;
    }
    app_message_outbox_send();
    //  Log will tell if discovery request was sent.
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Sent Discovery Initialization");
}

static void bulb_change_state (int index) {

}

// You can capture when the user selects a menu icon with a menu item select callback.
static void menu_select_callback (int index, void *ctx) {
    bulb_change_state(index);
}

//  Pebble app receives dictionary containing bulb info, etc.
static void process_bulb_network_data (DictionaryIterator *iter) {

    APP_LOG(APP_LOG_LEVEL_DEBUG, "Initiate processing of bulb network");


    numberOfBulbs = dict_find(iter, 1)->value->uint8;

    bulbList = malloc(sizeof(bulb)*numberOfBulbs);

    int j = 2;
    for (int i = 0; i < numberOfBulbs; i++) {
        bulbList[i] = (Bulb) {
            .label = (char*)dict_find(iter, j++)->value,
            .state = (uint8_t)dict_find(iter, j++)->value,
            .brightness = (uint16_t)dict_find(iter, j++)->value,
            .color = (uint16_t)dict_find(iter, j++)->value,
        };
    }

    // Little maggot array.
    char bulbNames[numberOfBulbs][32];

    bulb_menu = malloc(sizeof(SimpleMenuItem) * numberOfBulbs);

    all_bulbs[0] = (SimpleMenuItem){
        // You should give each menu item a title and callback
        .title = "All Lights",
        .callback = menu_select_callback,
    };

    //  Create sections for the bulb names.
    int j = 2;
    for (int i = 0; i < numberOfBulbs; i++) {
        strcpy(bulbNames[i], (char*)dict_find(iter, i+2)->value);
        bulb_menu[i] = (SimpleMenuItem){
            .title = (char*)dict_find(iter, j++)->value,
            .subtitle = (char*)dict_find(iter, j++)-> value,
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

    // Here is where we will kill the loading screen.
    //loading_screen_destroy();
    // Now we prepare to initialize the simple menu layer
    // We need the bounds to specify the simple menu layer's viewport size
    // In this case, it'll be the same as the window's
    Layer *window_layer = window_get_root_layer(window);
    GRect bounds = layer_get_frame(window_layer);

    // Initialize the simple menu layer
    simple_menu_layer = simple_menu_layer_create(bounds, window, menu_sections, NUM_MENU_SECTIONS, NULL);

    // Add it to the window for display
    layer_add_child(window_layer, simple_menu_layer_get_layer(simple_menu_layer));
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Added menu layer!");
}

static void handle_receive (DictionaryIterator *iter, void *context) {
    int message_type = dict_read_first(iter)->value->uint8;
    APP_LOG(APP_LOG_LEVEL_DEBUG, "Received response from phone");
    switch(message_type) {
    case 0:
        APP_LOG(APP_LOG_LEVEL_DEBUG, "No Network Found - Key Code: %d", message_type);
        //display_no_network_found(); //TODO implement
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

// This initializes the menu upon window load
static void window_load (Window *window) {
    // SPLASH SCREEN HERE

}

// Deinitialize resources on window unload that were initialized on window load.
void window_unload (Window *window) {
    simple_menu_layer_destroy(simple_menu_layer);
  // Cleanup the menu icon
  //gbitmap_destroy(menu_icon_image);
}

int main(void) {

    //  Initialize app message inbox/outbox.
    app_message_init();

    //  Starts a loading screen until bulbs are finished being sent.
    //loading_screen_init();

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

    app_event_loop();

    window_destroy(window);
}

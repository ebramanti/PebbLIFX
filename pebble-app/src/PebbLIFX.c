#include "pebble.h"

#define NUM_MENU_SECTIONS 2

#define BULB_DISCOVERY_REQUEST_KEY 0

static Window *window;
static SimpleMenuLayer *simple_menu_layer;

// A simple menu layer can have multiple sections
static SimpleMenuSection menu_sections[NUM_MENU_SECTIONS];

int numberOfBulbs; 

static SimpleMenuItem all_bulbs[1];
static SimpleMenuItem* bulb_list;

//  Pebble app queries phone to start discoverer on phone.
static void bulb_discovery_init(void) {
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

// You can capture when the user selects a menu icon with a menu item select callback. Doesn't do anything currently.
static void menu_select_callback(int index, void *ctx) {
    // Here we just change the subtitle to a literal string
    //bulb_list[index].subtitle = "You've hit select here!";
    // Mark the layer to be updated
    layer_mark_dirty(simple_menu_layer_get_layer(simple_menu_layer));
}

//  Pebble app receives dictionary containing bulb info, etc.
static void process_bulb_network_data(DictionaryIterator *iter) {

    APP_LOG(APP_LOG_LEVEL_DEBUG, "Initiate processing of bulb network");

    numberOfBulbs = dict_find(iter, 1)->value->uint8;
    // Little maggot array.
    char bulbNames[numberOfBulbs][32];

    bulb_list = malloc(sizeof(SimpleMenuItem) * numberOfBulbs);

    all_bulbs[0] = (SimpleMenuItem){
        // You should give each menu item a title and callback
        .title = "All Lights",
        .callback = menu_select_callback,
    };

    //  Create sections for the bulb names.
    for (int i = 0; i < numberOfBulbs; i++) {
        strcpy(bulbNames[i], (char*)dict_find(iter, i+2)->value);
        bulb_list[i] = (SimpleMenuItem){
            .title = (char*)dict_find(iter, i+2)->value,
            .callback = menu_select_callback,
        };
        APP_LOG(APP_LOG_LEVEL_INFO, "Added bulb to menu: %s", bulb_list[i].title);
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
        .items = bulb_list,
    };

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

static void handle_receive(DictionaryIterator *iter, void *context) {
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
static void app_message_init(void) {
    app_message_open(app_message_inbox_size_maximum(), dict_calc_buffer_size(3, 3));
    app_message_register_inbox_received(handle_receive);
}

// This initializes the menu upon window load
static void window_load(Window *window) {
    // SPLASH SCREEN HERE

}

// Deinitialize resources on window unload that were initialized on window load.
void window_unload(Window *window) {
    simple_menu_layer_destroy(simple_menu_layer);
  // Cleanup the menu icon
  //gbitmap_destroy(menu_icon_image);
}

int main(void) {

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

    app_event_loop();

    window_destroy(window);
}

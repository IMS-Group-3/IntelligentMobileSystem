#include <Pozyx.h>
#include <Pozyx_definitions.h>

////////////////////////////////////////////////
////////////////// PARAMETERS //////////////////
////////////////////////////////////////////////

uint16_t remote_id = 0x672a; // the network ID of the remote device
bool remote = false;         // whether to use the given remote device for ranging

// Variables for localization
// anchor location will Will be adjusted during startup
int32_t coordinates[3];                                           // The array used to save and send the coordinates to the raspberry
const uint8_t num_anchors = 4;                                    // the number of anchors
uint16_t anchors[num_anchors] = {0x6716, 0x6721, 0x676a, 0x6742}; // the network id of the anchors
int32_t anchors_x[num_anchors] = {0, 0, 0, 0};                    // anchor x-coorindates in mm
int32_t anchors_y[num_anchors] = {0, 0, 0, 0};                    // anchor y-coordinates in mm
int32_t heights[num_anchors] = {350, 0, 0, 200};                      // anchor z-coordinates in mm

uint8_t algorithm = POZYX_POS_ALG_UWB_ONLY; // positioning algorithm to use. try POZYX_POS_ALG_TRACKING for fast moving objects. | POZYX_POS_ALG_UWB_ONLY is default
uint8_t dimension = POZYX_3D;               // positioning dimension
int32_t height = 150;                        // height of device, required in 2.5D positioning

// variables for sampling distance average

const uint8_t num_samples = 75;
int32_t distance_buffer[2][num_samples];
int buffer_index[2] = {0, 0};
int num_readings[2] = {0, 0};
int8_t case_find_distance = 0;
uint16_t destination_id[2] = {0x6721, 0x6742}; // the network id of the other pozyx devices

uint8_t ranging_protocol = POZYX_RANGE_PROTOCOL_PRECISION; // ranging protocol of the Pozyx.

////////////////////////////////////////////////

void setup()
{
  Serial.begin(115200);

  if (Pozyx.begin() == POZYX_FAILURE)
  {
    Serial.println("ERROR: Unable to connect to POZYX shield");
    Serial.println("Reset required");
    delay(100);
    abort();
  }
  // setting the remote_id back to NULL will use the local Pozyx
  if (!remote)
  {
    remote_id = NULL;
  }
  Serial.println("------------POZYX IMS-3------------");
  Serial.println("NOTES:");
  Serial.println("Start the system with the remote device @origin base station: ");

  // set the ranging protocol
  Pozyx.setRangingProtocol(ranging_protocol, remote_id);

  while (case_find_distance < 2)
  { // Poll for the X & Y range upon start
    pollRange();
  }

  Serial.println(F("Performing manual anchor configuration:"));
  Serial.print("Anchor 0: ");
  Serial.print(anchors_x[0]);
  Serial.print(", ");
  Serial.println(anchors_y[0]);

  Serial.print("Anchor 1: ");
  Serial.print(anchors_x[1]);
  Serial.print(", ");
  Serial.println(anchors_y[1]);

  Serial.print("Anchor 2: ");
  Serial.print(anchors_x[2]);
  Serial.print(", ");
  Serial.println(anchors_y[2]);

  Serial.print("Anchor 3: ");
  Serial.print(anchors_x[3]);
  Serial.print(", ");
  Serial.println(anchors_y[3]);
  // clear all previous devices in the device list
  Pozyx.clearDevices(remote_id);
  // sets the anchor manually
  setAnchorsManual();
  // sets the positioning algorithm
  Pozyx.setPositionAlgorithm(algorithm, dimension, remote_id);
  Serial.println("------------POZYX IMS-3------------");
  Serial.println("START Localization:");
  // pollLocation();
}
void loop()
{
  pollLocation();
}

void pollLocation()
{
  coordinates_t position;
  int status;
  status = Pozyx.doPositioning(&position, dimension, height, algorithm);

  if (status == POZYX_SUCCESS)
  {
    // prints out the result
    printCoordinates(position);
  }
  else
  {
    // prints out the error code
    printErrorCode("Positioning: ");
  }
}

// prints the coordinates for processing
void printCoordinates(coordinates_t coor)
{
  Serial.print("Position");
  Serial.print(",x:");
  Serial.print(coor.x);
  Serial.print(",y:");
  Serial.print(coor.y);
  Serial.print(",z:");
  Serial.println(coor.z);
}

void pollRange()
{
  device_range_t range;
  int32_t distance_cm;
  int status = 0;
  int32_t avg_distance = 0;

  status = Pozyx.doRanging(destination_id[case_find_distance], &range);

  if (status == POZYX_SUCCESS)
  {
    distance_cm = round(range.distance / 10.0);
    if (distance_cm != 0)
    {
      updateBuffer(distance_cm, case_find_distance);
    }
    avg_distance = findAverageDistance(case_find_distance);
    if (avg_distance != 0)
    {
      Serial.print(case_find_distance == 0 ? "Distance Y=" : "Distance X=");
      int avg_distance_mm = avg_distance * 10;
      Serial.println(avg_distance);
      if (case_find_distance == 0)
      {
        anchors_y[1] = avg_distance_mm;
        anchors_y[2] = avg_distance_mm;
      }
      else
      {
        anchors_x[2] = avg_distance_mm;
        anchors_x[3] = avg_distance_mm;
      }
      memset(distance_buffer[case_find_distance], 0, sizeof(distance_buffer[case_find_distance]));
      buffer_index[case_find_distance] = 0;
      num_readings[case_find_distance] = 0;
      case_find_distance = (case_find_distance + 1);
    }
  }
  else
  {
    Serial.println("ERROR: ranging");
  }
}

void updateBuffer(int32_t distance_cm, int device_index)
{
  distance_buffer[device_index][buffer_index[device_index]] = distance_cm;
  buffer_index[device_index] = (buffer_index[device_index] + 1) % num_samples;
  if (num_readings[device_index] < num_samples)
  {
    num_readings[device_index]++;
  }
}

int32_t findAverageDistance(int device_index)
{
  if (num_readings[device_index] < num_samples)
  {
    return 0;
  }

  int32_t sum = 0;
  for (int i = 0; i < num_readings[device_index]; i++)
  {
    sum += distance_buffer[device_index][i];
  }
  return sum / num_readings[device_index];
}

// function to manually set the anchor coordinates
void setAnchorsManual()
{
  for (int i = 0; i < num_anchors; i++)
  {
    device_coordinates_t anchor;
    anchor.network_id = anchors[i];
    anchor.flag = 0x1;
    anchor.pos.x = anchors_x[i];
    anchor.pos.y = anchors_y[i];
    anchor.pos.z = heights[i];
    Pozyx.addDevice(anchor, remote_id);
  }
  if (num_anchors > 4)
  {
    Pozyx.setSelectionOfAnchors(POZYX_ANCHOR_SEL_AUTO, num_anchors, remote_id);
  }
}

// error printing function for debugging
void printErrorCode(String operation)
{
  uint8_t error_code;
  if (remote_id == NULL)
  {
    Pozyx.getErrorCode(&error_code);
    Serial.print("ERROR ");
    Serial.print(operation);
    Serial.print(", local error code: 0x");
    Serial.println(error_code, HEX);
    return;
  }
  int status = Pozyx.getErrorCode(&error_code, remote_id);
  if (status == POZYX_SUCCESS)
  {
    Serial.print("ERROR ");
    Serial.print(operation);
    Serial.print(" on ID 0x");
    Serial.print(remote_id, HEX);
    Serial.print(", error code: 0x");
    Serial.println(error_code, HEX);
  }
  else
  {
    Pozyx.getErrorCode(&error_code);
    Serial.print("ERROR ");
    Serial.print(operation);
    Serial.print(", couldn't retrieve remote error code, local error: 0x");
    Serial.println(error_code, HEX);
  }
}

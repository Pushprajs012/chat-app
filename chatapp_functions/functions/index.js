/* eslint-disable no-undef */
/* eslint-disable no-const-assign */
/* eslint-disable no-trailing-spaces */
/* eslint-disable camelcase */
/* eslint-disable max-len */
const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.messageNotification = functions.database.ref("/notifications/{receiver_user_id}/{sender_user_id}/{chat_id}").onCreate((snap, context) => {
  const receiver_user_id = context.params.receiver_user_id;
  const sender_user_id = context.params.sender_user_id;
  const chat_id = context.params.chat_id;

  const getMessage = admin.database().ref(`/notifications/${receiver_user_id}/${sender_user_id}/${chat_id}/message`).once("value");
  return getMessage.then((result) => {
    const message = result.val();

    const deviceToken = admin.database().ref(`/users/${receiver_user_id}/device_token`).once("value");

    return deviceToken.then((result) => {
      const token_id = result.val();

      const following_username = admin.database().ref(`/users/${sender_user_id}/username`).once("value");

      return following_username.then((result) => {
        const username = result.val();

        const following_profile_image = admin.database().ref(`/users/${sender_user_id}/profile_image`).once("value");

        return following_profile_image.then((result) => {
          const image = result.val();
          const payload = {
            "data": {
              "from_user_id": sender_user_id,
              "to_user_id": receiver_user_id,
              "title": username,
              "body": message,
              "type": "message_notification",
              "icon": "https://firebasestorage.googleapis.com/v0/b/chatapp-a8e7b.appspot.com/o/user.png?alt=media&token=3579c02c-3527-4e8d-ac0d-72a98600777c",
            },
          };

          return admin.messaging().sendToDevice(token_id, payload).then((response) => {
            return console.log(username + " sent you a message " + message + " " + image + " " + sender_user_id + " " + receiver_user_id);
          });
        });
      });
    });
  });
});

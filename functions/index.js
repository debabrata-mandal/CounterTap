const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();

exports.notifyNewOrder = onDocumentCreated(
  "tenants/{tenantId}/orders/{orderId}",
  async (event) => {
    const order = event.data.data();
    const tenantId = event.params.tenantId;
    const orderId = event.params.orderId;

    const db = getFirestore();

    // Get tenant owner UID
    const tenantDoc = await db.collection("tenants").doc(tenantId).get();
    if (!tenantDoc.exists) return;
    const ownerId = tenantDoc.data().ownerId;
    if (!ownerId) return;

    // Get FCM token
    const userDoc = await db.collection("users").doc(ownerId).get();
    if (!userDoc.exists) return;
    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) return;

    // Send notification
    const customerName = order.customerName || "A customer";
    const total = Math.round(order.totalAmount || 0);

    await getMessaging().send({
      token: fcmToken,
      notification: {
        title: "New Order Received!",
        body: `${customerName} ordered ₹${total}`,
      },
      data: {
        tenantId,
        orderId,
      },
      android: {
        priority: "high",
        notification: {
          channelId: "orders",
          sound: "default",
        },
      },
    });
  }
);

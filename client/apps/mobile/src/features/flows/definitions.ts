import {operations} from '@kuaixie/sdk';
import type {ApiOperation} from '@kuaixie/sdk';
export interface FlowDefinition { id:string; titleKey:string; descKey:string; operations: ApiOperation[]; }
const definitions:Record<string,{title:string;description:string;paths:string[]}>={
  "buyer-verify": {
    "title": "buyer_verify",
    "description": "buyer_verify",
    "paths": [
      "/rpc/submit_buyer_kyc"
    ]
  },
  "carrier-verify": {
    "title": "carrier_verify",
    "description": "carrier_verify",
    "paths": [
      "/rpc/batch12/submit_carrier_kyc"
    ]
  },
  "enterprise-verify": {
    "title": "enterprise_verify",
    "description": "enterprise_verify",
    "paths": [
      "/rpc/batch12/submit_enterprise_verify"
    ]
  },
  "account-security": {
    "title": "account_security",
    "description": "account_security",
    "paths": [
      "/rpc/identity/change_password",
      "/rpc/batch12/request_verification_code",
      "/rpc/verify_contact_code",
      "/rpc/identity/rebind_contact",
      "/rpc/identity/reset_own_password_by_code"
    ]
  },
  "kyc-docs": {
    "title": "kyc_docs",
    "description": "kyc_docs",
    "paths": [
      "/rpc/issue_face_challenge",
      "/rpc/record_face_verification",
      "/rpc/upsert_kyc_doc_with_challenge"
    ]
  },
  "pk-quotes": {
    "title": "pk_quotes",
    "description": "pk_quotes",
    "paths": [
      "/rpc/submit_pk_quote",
      "/rpc/award_pk",
      "/rpc/close_pk"
    ]
  },
  "order-actions": {
    "title": "order_actions",
    "description": "order_actions",
    "paths": [
      "/rpc/get_order_evidence",
      "/rpc/get_peer_profile",
      "/rpc/open_peer_thread"
    ]
  },
  "purchase-proof": {
    "title": "purchase_proof",
    "description": "purchase_proof",
    "paths": [
      "/rpc/confirm_purchase"
    ]
  },
  "boarding-proof": {
    "title": "boarding_proof",
    "description": "boarding_proof",
    "paths": [
      "/rpc/record_boarding_proof",
      "/rpc/record_arrival_proof"
    ]
  },
  "shipping-proof": {
    "title": "shipping_proof",
    "description": "shipping_proof",
    "paths": [
      "/rpc/confirm_ship"
    ]
  },
  "delivery-proof": {
    "title": "delivery_proof",
    "description": "delivery_proof",
    "paths": [
      "/rpc/record_delivery_proof"
    ]
  },
  "cancel-request": {
    "title": "cancel_request",
    "description": "cancel_request",
    "paths": [
      "/rpc/request_cancel_v2",
      "/rpc/request_no_boarding_refund"
    ]
  },
  "invoice-request": {
    "title": "invoice_request",
    "description": "invoice_request",
    "paths": [
      "/rpc/request_invoice"
    ]
  },
  "assist": {
    "title": "assist",
    "description": "assist",
    "paths": [
      "/rpc/batch12/create_assist_request",
      "/rpc/batch12/accept_assist",
      "/rpc/batch12/submit_assist_handover_a",
      "/rpc/batch12/finish_assist",
      "/rpc/batch12/cancel_assist"
    ]
  },
  "invite": {
    "title": "invite",
    "description": "invite",
    "paths": [
      "/rpc/get_invite_code_owner",
      "/rpc/consume_invite_code",
      "/rpc/get_operator_invite_code",
      "/rpc/list_team_members"
    ]
  },
  "meetup": {
    "title": "meetup",
    "description": "meetup",
    "paths": [
      "/rpc/issue_meetup_code",
      "/rpc/confirm_meetup"
    ]
  },
  "shipping-fee": {
    "title": "shipping_fee",
    "description": "shipping_fee",
    "paths": [
      "/rpc/propose_shipping_fee",
      "/rpc/respond_shipping_fee"
    ]
  },
  "privacy-call": {
    "title": "privacy_call",
    "description": "privacy_call",
    "paths": [
      "/rpc/batch12/get_or_bind_privacy_number",
      "/rpc/log_privacy_call"
    ]
  },
  "order-call": {
    "title": "order_call",
    "description": "order_call",
    "paths": [
      "/rpc/batch12/start_order_call",
      "/rpc/batch12/accept_order_call",
      "/rpc/batch12/reject_order_call",
      "/rpc/batch12/hangup_order_call"
    ]
  },
  "community-comment": {
    "title": "community_comment",
    "description": "community_comment",
    "paths": [
      "/rpc/batch12/add_post_comment",
      "/rpc/batch12/screen_chat_content"
    ]
  },
  "deposit": {
    "title": "deposit",
    "description": "deposit",
    "paths": [
      "/rpc/create_publish_deposit",
      "/rpc/refund_publish_deposit"
    ]
  }
};
export const flowDefinitions:Record<string,FlowDefinition>=Object.fromEntries(
  Object.entries(definitions).map(([id,d])=>[id,{id,titleKey:`fast.flow.${d.title}`,descKey:`fast.description.${d.description}`,operations:d.paths.map(path=>{const op=operations.find(o=>o.path===path);if(!op)throw new Error(`UNMAPPED_BACKEND_OPERATION: ${path}`);return op;})}])
);
export function getFlow(id:string):FlowDefinition { const result=flowDefinitions[id]; if(!result)throw new Error(`UNKNOWN_FLOW: ${id}`); return result; }
export const flowIds=Object.keys(flowDefinitions);

export function navigate(page:'route'|'discover'|'tools'|'me'|'index'|'demand-pool'|'publish-hub'|'messages'){
 const url=`/pages/${page}/index`;
 if(['index','demand-pool','publish-hub','messages','me'].includes(page))uni.switchTab({url});
 else uni.navigateTo({url});
}

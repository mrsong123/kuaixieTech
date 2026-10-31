package com.kuaixietech.mapper.trading;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface TradeRpcMapper {
  Map<String,Object> lockOrder(@Param("id")String id);
  Map<String,Object> lockDemand(@Param("id")String id);
  Map<String,Object> lockProduct(@Param("id")String id);
  Map<String,Object> quote(@Param("qid")String qid,@Param("demandId")String demandId);
  List<Map<String,Object>> pendingQuotes(@Param("demandId")String demandId);
  int ownPendingQuoteCount(@Param("demandId")String demandId,@Param("uid")String uid);
  Map<String,Object> pendingOwnQuote(@Param("demandId")String demandId,@Param("uid")String uid);
  int pkWindowExpired(@Param("demandId")String demandId);
  int pkWindowOpen(@Param("demandId")String demandId);
  int isCarrier(@Param("uid")String uid);
  int isAdmin(@Param("uid")String uid);
  int lineMatches(@Param("uid")String uid,@Param("tripType")String tripType,@Param("direction")String direction);
  int acceptOrder(@Param("id")String id,@Param("uid")String uid,@Param("deposit")BigDecimal deposit);
  int putDeposit(@Param("id")String id,@Param("orderId")String orderId,@Param("payer")String payer,
      @Param("role")String role,@Param("kind")String kind,@Param("amount")BigDecimal amount);
  int addMessage(@Param("id")String id,@Param("orderId")String orderId,@Param("sender")String sender,@Param("content")String content);
  int addNotice(@Param("id")String id,@Param("uid")String uid,@Param("kind")String kind,@Param("title")String title,
     @Param("body")String body,@Param("orderId")String orderId);
  int updatePendingQuote(@Param("qid")String qid,@Param("amount")BigDecimal amount,@Param("days")int days,
     @Param("place")String place,@Param("note")String note);
  int insertQuote(@Param("qid")String qid,@Param("demandId")String demandId,@Param("uid")String uid,
     @Param("amount")BigDecimal amount,@Param("days")int days,@Param("place")String place,@Param("note")String note);
  int setPkResult(@Param("demandId")String demandId,@Param("json")String json);
  int countOpenOrders(@Param("demandId")String demandId);
  int insertPkOrder(@Param("id")String id,@Param("orderNo")String orderNo,@Param("buyer")String buyer,
     @Param("vendor")String vendor,@Param("demand")String demand,@Param("itemName")String itemName,
     @Param("goods")BigDecimal goods,@Param("fee")BigDecimal fee,@Param("buyerDeposit")BigDecimal buyerDeposit,
     @Param("vendorDeposit")BigDecimal vendorDeposit,@Param("insured")String insured,
     @Param("premium")BigDecimal premium,@Param("declared")BigDecimal declared,
     @Param("shipFee")BigDecimal shipFee,@Param("shipStatus")String shipStatus);
  int quoteWon(@Param("id")String id);
  int remainingQuotesLost(@Param("demandId")String demandId,@Param("quoteId")String quoteId);
  int awardDemand(@Param("demandId")String demandId,@Param("quoteId")String quoteId);
  int createPublishDeposit(@Param("id")String id,@Param("uid")String uid,@Param("amount")BigDecimal amount,
       @Param("source")String source,@Param("orderId")String orderId);
  int unfinishedOrders(@Param("uid")String uid);
  BigDecimal sumFrozenPublish(@Param("uid")String uid);
  int refundPublish(@Param("uid")String uid);
  int productStock(@Param("id")String id,@Param("stock")int stock);
  int appendNode(@Param("id")String id,@Param("orderId")String orderId,@Param("uid")String uid,
      @Param("role")String role,@Param("node")String node,@Param("detail")String detail);
}

/*   1:    */package tau.tac.adx.agents;
/*   2:    */
/*   3:    */import adx.logging.AdNetworkReportFormatter;
/*   4:    */import adx.logging.AdNetworkReportSorted;
/*   5:    */import adx.logging.BidBundleFormatter;
/*   6:    */import adx.query.IQuerySelect;
/*   7:    */import adx.query.IQuerySelectAgg;
/*   8:    */import adx.query.IQuerySelectAggStar;
/*   9:    */import adx.query.IQuerySelectStar;
/*  10:    */import adx.query.Query;
/*  11:    */import adx.stats.CampaignStrategy;
/*  12:    */import adx.stats.LoadBalancer;
/*  13:    */import adx.stats.Neuro;
/*  14:    */import adx.stats.Users;
/*  15:    */import edu.umich.eecs.tac.props.Ad;
/*  16:    */import edu.umich.eecs.tac.props.BankStatus;
/*  17:    */import java.util.HashMap;
/*  18:    */import java.util.HashSet;
/*  19:    */import java.util.Iterator;
/*  20:    */import java.util.LinkedList;
/*  21:    */import java.util.TreeMap;
/*  22:    */import java.util.logging.Level;
/*  23:    */import se.sics.isl.transport.Transportable;
/*  24:    */import se.sics.tasim.aw.Agent;
/*  25:    */import se.sics.tasim.aw.Message;
/*  26:    */import se.sics.tasim.props.SimulationStatus;
/*  27:    */import se.sics.tasim.props.StartInfo;
/*  28:    */import tau.tac.adx.ads.properties.AdType;
/*  29:    */import tau.tac.adx.demand.CampaignStats;
/*  30:    */import tau.tac.adx.devices.Device;
/*  31:    */import tau.tac.adx.props.AdxBidBundle;
/*  32:    */import tau.tac.adx.props.AdxQuery;
/*  33:    */import tau.tac.adx.props.PublisherCatalog;
/*  34:    */import tau.tac.adx.props.PublisherCatalogEntry;
/*  35:    */import tau.tac.adx.report.adn.AdNetworkKey;
/*  36:    */import tau.tac.adx.report.adn.AdNetworkReport;
/*  37:    */import tau.tac.adx.report.adn.AdNetworkReportEntry;
/*  38:    */import tau.tac.adx.report.demand.AdNetBidMessage;
/*  39:    */import tau.tac.adx.report.demand.AdNetworkDailyNotification;
/*  40:    */import tau.tac.adx.report.demand.CampaignOpportunityMessage;
/*  41:    */import tau.tac.adx.report.demand.CampaignReport;
/*  42:    */import tau.tac.adx.report.demand.CampaignReportEntry;
/*  43:    */import tau.tac.adx.report.demand.CampaignReportKey;
/*  44:    */import tau.tac.adx.report.demand.InitialCampaignMessage;
/*  45:    */import tau.tac.adx.report.publisher.AdxPublisherReport;
/*  46:    */import tau.tac.adx.report.publisher.AdxPublisherReportEntry;
/*  47:    */import tau.tac.adx.users.AdxUser;
/*  48:    */import tau.tac.adx.users.properties.Age;
/*  49:    */import tau.tac.adx.users.properties.Gender;
/*  50:    */import tau.tac.adx.users.properties.Income;
/*  51:    */
/*  52:    */public class Agent00 extends Agent
/*  53:    */{
/*  54: 54 */  private final java.util.logging.Logger log = java.util.logging.Logger.getLogger(Agent00.class.getName());
/*  55:    */  
/*  60:    */  private AdNetworkDailyNotification adNetworkDailyNotification;
/*  61:    */  
/*  66:    */  private String demandAgentAddress;
/*  67:    */  
/*  72:    */  private String adxAgentAddress;
/*  73:    */  
/*  78:    */  private CampaignData pendingCampaign;
/*  79:    */  
/*  83: 83 */  private java.util.Map<Integer, CampaignData> myCampaigns = new TreeMap();
/*  84:    */  
/*  87:    */  double ucsBid;
/*  88:    */  
/*  91:    */  double ucsBidMin;
/*  92:    */  
/*  95:    */  double ucsBidMax;
/*  96:    */  
/*  99:    */  double ucsLevel;
/* 100:    */  
/* 102:    */  private int day;
/* 103:    */  
/* 105:    */  private boolean wonLastCampagin;
/* 106:    */  
/* 108:    */  private double factor;
/* 109:    */  
/* 111:    */  private double qualityRating;
/* 112:    */  
/* 115:    */  private static class Mutable<T>
/* 116:    */  {
/* 117:    */    public T value;
/* 118:    */    
/* 121:    */    Mutable(T defaultValue)
/* 122:    */    {
/* 123:123 */      this.value = defaultValue;
/* 124:    */    }
/* 125:    */  }
/* 126:    */  
/* 133:133 */  private java.util.Map<Integer, Mutable<Double>> campaignEstTargetImps = new TreeMap();
/* 134:    */  
/* 137:    */  protected void messageReceived(Message message)
/* 138:    */  {
/* 139:    */    try
/* 140:    */    {
/* 141:141 */      Transportable content = message.getContent();
/* 142:    */      
/* 143:143 */      if ((content instanceof InitialCampaignMessage)) {
/* 144:144 */        handleInitialCampaignMessage((InitialCampaignMessage)content);
/* 145:    */      }
/* 146:146 */      else if ((content instanceof CampaignOpportunityMessage)) {
/* 147:147 */        handleICampaignOpportunityMessage((CampaignOpportunityMessage)content);
/* 148:    */      }
/* 149:149 */      else if ((content instanceof CampaignReport)) {
/* 150:150 */        handleCampaignReport((CampaignReport)content);
/* 151:    */      }
/* 152:152 */      else if ((content instanceof AdNetworkDailyNotification)) {
/* 153:153 */        handleAdNetworkDailyNotification((AdNetworkDailyNotification)content);
/* 154:    */      }
/* 155:155 */      else if ((content instanceof AdxPublisherReport)) {
/* 156:156 */        handleAdxPublisherReport((AdxPublisherReport)content);
/* 157:    */      }
/* 158:158 */      else if ((content instanceof SimulationStatus)) {
/* 159:159 */        handleSimulationStatus((SimulationStatus)content);
/* 160:    */      }
/* 161:161 */      else if ((content instanceof PublisherCatalog)) {
/* 162:162 */        handlePublisherCatalog((PublisherCatalog)content);
/* 163:    */      }
/* 164:164 */      else if ((content instanceof AdNetworkReport)) {
/* 165:165 */        handleAdNetworkReport((AdNetworkReport)content);
/* 166:    */      }
/* 167:167 */      else if ((content instanceof StartInfo)) {
/* 168:168 */        handleStartInfo((StartInfo)content);
/* 169:    */      }
/* 170:170 */      else if ((content instanceof BankStatus)) {
/* 171:171 */        handleBankStatus((BankStatus)content);
/* 172:    */      }
/* 173:    */      else {
/* 174:174 */        this.log.info("UNKNOWN Message Received: " + content);
/* 175:    */      }
/* 176:    */    }
/* 177:    */    catch (NullPointerException e) {
/* 178:178 */      e.printStackTrace();
/* 179:179 */      this.log.log(Level.SEVERE, 
/* 180:180 */        "Exception thrown while trying to parse message." + e);
/* 181:181 */      return;
/* 182:    */    }
/* 183:    */  }
/* 184:    */  
/* 185:    */  private void handleBankStatus(BankStatus content) {
/* 186:186 */    this.log.info("Day " + this.day + ":" + content.toString());
/* 187:    */  }
/* 188:    */  
/* 193:    */  protected void handleStartInfo(StartInfo startInfo) {}
/* 194:    */  
/* 199:    */  private void handlePublisherCatalog(PublisherCatalog publisherCatalog)
/* 200:    */  {
/* 201:201 */    HashSet<String> publishers = new HashSet();
/* 202:    */    
/* 203:203 */    for (PublisherCatalogEntry entry : publisherCatalog) {
/* 204:204 */      publishers.add(entry.getPublisherName());
/* 205:    */    }
/* 206:    */    
/* 208:208 */    Users.getInstance().initialize(publishers);
/* 209:    */    
/* 211:211 */    LoadBalancer.getInstance().initialize();
/* 212:    */    
/* 214:214 */    Neuro.getInstance().initialize(publishers);
/* 215:    */  }
/* 216:    */  
/* 224:    */  private void handleInitialCampaignMessage(InitialCampaignMessage campaignMessage)
/* 225:    */  {
/* 226:226 */    this.log.info(campaignMessage.toString());
/* 227:    */    
/* 228:228 */    this.day = 0;
/* 229:    */    
/* 230:230 */    this.demandAgentAddress = campaignMessage.getDemandAgentAddress();
/* 231:231 */    this.adxAgentAddress = campaignMessage.getAdxAgentAddress();
/* 232:    */    
/* 233:233 */    CampaignData campaignData = new CampaignData(campaignMessage);
/* 234:234 */    campaignData.setBudget(campaignMessage.getReachImps().longValue() / 1000.0D * this.factor);
/* 235:    */    
/* 240:240 */    this.log.info("Day " + this.day + ": Allocated campaign - " + campaignData);
/* 241:241 */    this.myCampaigns.put(Integer.valueOf(campaignMessage.getId()), campaignData);
/* 242:    */    
/* 244:244 */    this.campaignEstTargetImps.put(Integer.valueOf(campaignMessage.getId()), new Mutable(Double.valueOf(0.0D)));
/* 245:    */    
/* 247:247 */    LoadBalancer.getInstance().put(campaignData, (campaignData.getBudget() * 1000.0D), "self");
/* 248:    */  }
/* 249:    */  
/* 257:    */  private void handleICampaignOpportunityMessage(CampaignOpportunityMessage com)
/* 258:    */  {
/* 259:259 */    this.day = com.getDay();
/* 260:    */    
/* 261:261 */    this.pendingCampaign = new CampaignData(com);
/* 262:262 */    this.log.info("Day " + this.day + ": Campaign opportunity - " + this.pendingCampaign);
/* 263:    */    
/* 272:272 */    this.log.info("previous factor=" + this.factor);
/* 273:    */    
/* 275:275 */    if (this.wonLastCampagin) {
/* 276:276 */      this.factor += (this.factor - 0.1D) / 8.0D;
/* 277:    */    }
/* 278:    */    else {
/* 279:279 */      this.factor -= (this.factor - 0.1D) / 4.0D;
/* 280:    */    }
/* 281:    */    
/* 282:282 */    this.log.info("current factor=" + this.factor);
/* 283:    */    
/* 285:285 */    CampaignStrategy cs = new CampaignStrategy(this.day, this.myCampaigns, this.pendingCampaign, this.factor, this.qualityRating);
/* 286:286 */    Object[] offer = cs.makeBidOffer();
/* 287:287 */    long cmpBid = ((Long)offer[0]).longValue();
/* 288:288 */    this.factor = ((Double)offer[1]).doubleValue();
/* 289:    */    
/* 290:290 */    double cmpBidUnits = cmpBid / 1000.0D;
/* 291:    */    
/* 292:292 */    this.log.info("Day " + this.day + ": Campaign total budget bid: " + cmpBidUnits);
/* 293:    */    
/* 299:299 */    if (this.adNetworkDailyNotification != null) {
/* 300:300 */      double prevUcsLevel = this.ucsLevel;
/* 301:301 */      double prevUcsBid = this.ucsBid;
/* 302:    */      
/* 303:303 */      this.ucsLevel = this.adNetworkDailyNotification.getServiceLevel();
/* 304:    */      
/* 305:305 */      if (this.ucsLevel >= 0.8D) {
/* 306:306 */        this.ucsBidMax -= (this.ucsBidMax - this.ucsBid) / 4.0D;
/* 307:307 */        this.ucsBid -= (this.ucsBid - this.ucsBidMin) / 2.0D;
/* 308:    */      }
/* 309:    */      else {
/* 310:310 */        this.ucsBidMin += (this.ucsBid - this.ucsBidMin) / 4.0D;
/* 311:311 */        this.ucsBid += (this.ucsBidMax - this.ucsBid) / 2.0D;
/* 312:    */        
/* 313:313 */        if ((prevUcsLevel == this.ucsLevel) && (this.ucsLevel < 0.7D)) {
/* 314:314 */          this.ucsBidMax = Math.min(2.0D, this.ucsBidMax * 1.1D);
/* 315:    */        }
/* 316:    */      }
/* 317:    */      
/* 318:318 */      this.ucsBid = 0.1D;
/* 319:    */      
/* 320:320 */      this.log.info("Day " + this.day + 
/* 321:321 */        ": Adjusting ucs bid: was " + prevUcsBid + 
/* 322:322 */        " level reported: " + this.ucsLevel + 
/* 323:323 */        " target: " + 1.0D + 
/* 324:324 */        " adjusted: " + this.ucsBid);
/* 325:    */    }
/* 326:    */    else {
/* 327:327 */      this.log.info("Day " + this.day + ": Initial ucs bid is " + this.ucsBid);
/* 328:    */    }
/* 329:    */    
/* 331:331 */    AdNetBidMessage bids = new AdNetBidMessage(this.ucsBid, this.pendingCampaign.id, Long.valueOf(cmpBid));
/* 332:332 */    sendMessage(this.demandAgentAddress, bids);
/* 333:    */  }
/* 334:    */  
/* 342:    */  private void handleAdNetworkDailyNotification(AdNetworkDailyNotification notificationMessage)
/* 343:    */  {
/* 344:344 */    this.adNetworkDailyNotification = notificationMessage;
/* 345:    */    
/* 346:346 */    this.log.info("Day " + this.day + ": Daily notification for campaign " + this.adNetworkDailyNotification.getCampaignId());
/* 347:    */    
/* 348:348 */    String campaignAllocatedTo = " allocated to " + notificationMessage.getWinner();
/* 349:349 */    String winner = notificationMessage.getWinner();
/* 350:350 */    this.wonLastCampagin = false;
/* 351:    */    
/* 352:352 */    long reasonableBudget = (this.pendingCampaign.getReachImps().longValue() * this.factor * Math.max(this.qualityRating, 1.0D) * 1.1D);
/* 353:    */    
/* 354:354 */    if ((this.pendingCampaign.id == this.adNetworkDailyNotification.getCampaignId()) && (notificationMessage.getCostMillis() != 0L))
/* 355:    */    {
/* 357:357 */      if (notificationMessage.getCostMillis() / this.pendingCampaign.getReachImps().longValue() > 0.5D) {
/* 358:358 */        reasonableBudget = (reasonableBudget * 1.5D);
/* 359:    */      }
/* 360:    */      
/* 362:362 */      this.pendingCampaign.setBudget(notificationMessage.getCostMillis() / 1000.0D);
/* 363:    */      
/* 364:364 */      this.myCampaigns.put(Integer.valueOf(this.pendingCampaign.id), this.pendingCampaign);
/* 365:    */      
/* 367:367 */      this.campaignEstTargetImps.put(Integer.valueOf(this.pendingCampaign.id), new Mutable(Double.valueOf(0.0D)));
/* 368:    */      
/* 369:369 */      campaignAllocatedTo = " WON at cost " + notificationMessage.getCostMillis();
/* 370:370 */      winner = "self";
/* 371:371 */      this.wonLastCampagin = true;
/* 372:    */    }
/* 373:    */    
/* 374:374 */    LoadBalancer.getInstance().put(this.pendingCampaign, reasonableBudget, winner);
/* 375:    */    
/* 380:380 */    this.log.info("Day " + this.day + ": " + campaignAllocatedTo + 
/* 381:381 */      ". UCS Level set to " + notificationMessage.getServiceLevel() + 
/* 382:382 */      " at price " + notificationMessage.getPrice() + 
/* 383:383 */      " Quality Score is: " + notificationMessage.getQualityScore());
/* 384:    */    
/* 385:385 */    this.qualityRating = notificationMessage.getQualityScore();
/* 386:    */  }
/* 387:    */  
/* 392:    */  private void handleSimulationStatus(SimulationStatus simulationStatus)
/* 393:    */  {
/* 394:394 */    this.log.info("Day " + this.day + " : Simulation Status Received");
/* 395:395 */    sendBidAndAds();
/* 396:396 */    this.log.info("Day " + this.day + " ended. Starting next day");
/* 397:397 */    this.day += 1;
/* 398:    */    
/* 400:400 */    LoadBalancer.getInstance().advanceDay();
/* 401:    */  }
/* 402:    */  
/* 406:    */  protected void sendBidAndAds()
/* 407:    */  {
/* 408:408 */    AdxBidBundle bidBundle = new AdxBidBundle();
/* 409:    */    
/* 411:411 */    BidBundleFormatter formatter = new BidBundleFormatter();
/* 412:    */    
/* 413:413 */    for (CampaignData campaign : this.myCampaigns.values())
/* 414:    */    {
/* 415:415 */      int dayBiddingFor = this.day + 1;
/* 416:    */      
/* 418:418 */      double impsToGo = campaign.reachImps.longValue() * 1.25D - ((Double)((Mutable)this.campaignEstTargetImps.get(Integer.valueOf(campaign.id))).value).doubleValue();
/* 419:    */      
/* 420:420 */      double reachRatio = ((Double)((Mutable)this.campaignEstTargetImps.get(Integer.valueOf(campaign.id))).value).doubleValue() / campaign.reachImps.longValue();
/* 421:    */      
/* 422:422 */      if (dayBiddingFor <= campaign.dayEnd + 1L) {
/* 423:423 */        this.log.info("[ campaign " + campaign.id + " ] reachRatio: " + reachRatio);
/* 424:    */      }
/* 425:    */      
/* 431:431 */      if ((dayBiddingFor >= campaign.dayStart) && (dayBiddingFor <= campaign.dayEnd))
/* 432:    */      {
/* 434:434 */        double rbid = 1000.0D * 
/* 435:    */        
/* 437:437 */          ((Double)((Object[])Query.select(LoadBalancer.getInstance().get(campaign.targetSegment)).property("avgDailyBudget").max().exec().iterator().next())[0]).doubleValue();
/* 438:438 */        rbid /= this.factor;
/* 439:    */        
/* 440:440 */        if (impsToGo <= 0.0D) {
/* 441:441 */          rbid = 0.0D;
/* 442:    */        }
/* 443:    */        
/* 445:445 */        Iterable<Object[]> users = Users.getInstance().generatePriorities(campaign.targetSegment);
/* 446:    */        
/* 448:448 */        Object[] ratios = (Object[])Query.select(users).index(6).sum().index(6).count().exec().iterator().next();
/* 449:449 */        double ratio = ((Integer)ratios[1]).intValue() / ((Double)ratios[0]).doubleValue();
/* 450:    */        
/* 451:451 */        for (Object[] user : users) {
/* 452:452 */          AdxQuery query = new AdxQuery(
/* 453:453 */            (String)user[3], 
/* 454:454 */            new AdxUser((Age)user[0], (Gender)user[1], (Income)user[2], 0.0D, 0), 
/* 455:455 */            (Device)user[4], 
/* 456:456 */            (AdType)user[5]);
/* 457:    */          
/* 458:458 */          double rbid_adjusted = 
/* 459:459 */            rbid * 
/* 460:460 */            Math.pow(
/* 461:461 */            ((Double)user[6]).doubleValue() * 
/* 462:462 */            ratio * (
/* 463:463 */            (Device)user[4] == Device.mobile ? campaign.getMobileCoef() : 1.0D) * (
/* 464:464 */            (AdType)user[5] == AdType.video ? campaign.getVideoCoef() : 1.0D), 
/* 465:465 */            0.125D);
/* 466:    */          
/* 468:468 */          rbid_adjusted *= Neuro.getInstance().test((Age)user[0], (Gender)user[1], (Income)user[2], (Device)user[4], (AdType)user[5], (String)user[3]);
/* 469:    */          
/* 471:471 */          bidBundle.addQuery(query, rbid_adjusted, new Ad(null), campaign.id, 1);
/* 472:    */          
/* 474:474 */          if (rbid != 0.0D) {
/* 475:475 */            formatter.add(campaign.id, query, rbid_adjusted);
/* 476:    */          }
/* 477:    */        }
/* 478:    */        
/* 480:480 */        double impressionLimit = Math.max(0.0D, impsToGo);
/* 481:    */        
/* 482:482 */        this.log.info("[ campaign " + campaign.id + " ] impressionLimit: " + impressionLimit / campaign.reachImps.longValue());
/* 483:    */        
/* 485:485 */        double budgetLimit = impressionLimit == 0.0D ? 0.0D : (1.25D - reachRatio) * rbid / 1000.0D * 1.2D;
/* 486:    */        
/* 488:488 */        bidBundle.setCampaignDailyLimit(campaign.id, (int)impressionLimit, budgetLimit);
/* 489:    */      }
/* 490:    */    }
/* 491:    */    
/* 492:492 */    for (String entry : formatter) {
/* 493:493 */      this.log.info(entry);
/* 494:    */    }
/* 495:    */    
/* 496:496 */    if (bidBundle != null) {
/* 497:497 */      this.log.info("Day " + this.day + ": Sending BidBundle");
/* 498:498 */      sendMessage(this.adxAgentAddress, bidBundle);
/* 499:    */    }
/* 500:    */  }
/* 501:    */  
/* 508:    */  private void handleCampaignReport(CampaignReport campaignReport)
/* 509:    */  {
/* 510:510 */    for (CampaignReportKey campaignKey : campaignReport.keys()) {
/* 511:511 */      int cmpId = campaignKey.getCampaignId().intValue();
/* 512:512 */      CampaignStats cstats = campaignReport.getCampaignReportEntry(
/* 513:513 */        campaignKey).getCampaignStats();
/* 514:514 */      ((CampaignData)this.myCampaigns.get(Integer.valueOf(cmpId))).setStats(cstats);
/* 515:    */      
/* 516:516 */      this.log.info("Day " + this.day + ": Updating campaign " + cmpId + " stats: " + 
/* 517:517 */        cstats.getTargetedImps() + " tgtImps " + 
/* 518:518 */        cstats.getOtherImps() + " nonTgtImps. Cost of imps is " + 
/* 519:519 */        cstats.getCost());
/* 520:    */      
/* 521:521 */      ((Mutable)this.campaignEstTargetImps.get(Integer.valueOf(cmpId))).value = Double.valueOf(cstats.getTargetedImps());
/* 522:    */    }
/* 523:    */  }
/* 524:    */  
/* 527:    */  private void handleAdxPublisherReport(AdxPublisherReport adxPublisherReport)
/* 528:    */  {
/* 529:529 */    LinkedList<Object[]> preferences = new LinkedList();
/* 530:    */    
/* 531:531 */    this.log.info("Publishers Report: ");
/* 532:532 */    for (PublisherCatalogEntry publisherKey : adxPublisherReport.keys()) {
/* 533:533 */      AdxPublisherReportEntry entry = (AdxPublisherReportEntry)adxPublisherReport.getEntry(publisherKey);
/* 534:534 */      this.log.info(entry.toString());
/* 535:    */      
/* 538:538 */      preferences.add(new Object[] {
/* 539:539 */        entry.getPublisherName(), 
/* 540:540 */        Double.valueOf(((Integer)entry.getAdTypeOrientation().get(AdType.video)).intValue()), 
/* 541:541 */        Double.valueOf(entry.getPopularity() + 1.0D) });
/* 542:    */    }
/* 543:    */    
/* 545:545 */    Users.getInstance().updateAdTypePreferences(preferences);
/* 546:    */  }
/* 547:    */  
/* 552:    */  private void handleAdNetworkReport(AdNetworkReport adnetReport)
/* 553:    */  {
/* 554:554 */    this.log.info("Day " + this.day + " : AdNetworkReport");
/* 555:    */    
/* 556:556 */    HashMap<Integer, Boolean> finished = new HashMap();
/* 557:    */    
/* 559:559 */    AdNetworkReportSorted entries = new AdNetworkReportSorted(adnetReport);
/* 560:560 */    AdNetworkReportFormatter formatter = new AdNetworkReportFormatter();
/* 561:    */    
/* 562:562 */    for (AdNetworkReportEntry entry : entries) {
/* 563:563 */      int campaignId = ((AdNetworkKey)entry.getKey()).getCampaignId();
/* 564:    */      
/* 565:565 */      if (this.myCampaigns.containsKey(Integer.valueOf(campaignId))) {
/* 566:566 */        formatter.add(entry);
/* 567:    */        
/* 572:572 */        AdType adType = ((AdNetworkKey)entry.getKey()).getAdType();
/* 573:573 */        Device device = ((AdNetworkKey)entry.getKey()).getDevice();
/* 574:574 */        CampaignData campaign = (CampaignData)this.myCampaigns.get(Integer.valueOf(campaignId));
/* 575:    */        
/* 576:576 */        Mutable<Double> d = (Mutable)this.campaignEstTargetImps.get(Integer.valueOf(campaignId)); Mutable<Double> 
/* 577:    */        
/* 578:578 */          tmp183_181 = d;
/* 579:579 */        tmp183_181.value = Double.valueOf(((Double)tmp183_181.value).doubleValue() + entry.getWinCount() * (
/* 580:580 */          adType == AdType.video ? campaign.getVideoCoef() : 1.0D) * (
/* 581:581 */          device == Device.mobile ? campaign.getMobileCoef() : 1.0D));
/* 582:    */        
/* 583:583 */        finished.put(Integer.valueOf(campaignId), Boolean.valueOf(false));
/* 584:    */      }
/* 585:    */    }
/* 586:    */    
/* 587:587 */    for (String entry : formatter) {
/* 588:588 */      this.log.info(entry);
/* 589:    */    }
/* 590:    */    
/* 591:591 */    for (Integer campaign : finished.keySet()) {
/* 592:592 */      finished.put(campaign, Boolean.valueOf(((CampaignData)this.myCampaigns.get(campaign)).reachImps.longValue() - ((Double)((Mutable)this.campaignEstTargetImps.get(campaign)).value).doubleValue() <= 0.0D));
/* 593:    */    }
/* 594:    */    
/* 595:595 */    Neuro.getInstance().update(entries, finished);
/* 596:    */  }
/* 597:    */  
/* 598:    */  protected void simulationSetup()
/* 599:    */  {
/* 600:600 */    this.adNetworkDailyNotification = null;
/* 601:601 */    this.myCampaigns.clear();
/* 602:602 */    this.ucsBid = 0.1D;
/* 603:603 */    this.ucsBidMin = 0.1D;
/* 604:604 */    this.ucsBidMax = 0.2D;
/* 605:605 */    this.ucsLevel = 1.0D;
/* 606:606 */    this.day = 0;
/* 607:607 */    this.wonLastCampagin = true;
/* 608:    */    
/* 609:609 */    this.factor = 0.1003125D;
/* 610:610 */    this.qualityRating = 1.0D;
/* 611:611 */    this.campaignEstTargetImps.clear();
/* 612:    */    
/* 613:613 */    this.log.fine("AdNet " + getName() + " simulationSetup");
/* 614:    */  }
/* 615:    */  
/* 616:    */  protected void simulationFinished()
/* 617:    */  {
/* 618:618 */    this.log.fine("Simulation Ended.");
/* 619:    */  }
/* 620:    */}


/* Location:           C:\Android\temp\agent00.jar
 * Qualified Name:     tau.tac.adx.agents.Agent00
 * JD-Core Version:    0.7.0-SNAPSHOT-20130630
 */
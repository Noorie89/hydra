//
//  RestoMenu.h
//  Hydra
//
//  Created by Pieter De Baets on 17/07/12.
//  Copyright (c) 2012 Zeus WPI. All rights reserved.
//

#import <Foundation/Foundation.h>

@class RKObjectMapping, RestoMenuItem;

@interface RestoMenu : NSObject <NSCoding>

@property (nonatomic, strong) NSDate *day;
@property (nonatomic) BOOL open;
@property (nonatomic, strong) NSArray *meat;
@property (nonatomic, strong) NSArray *vegetables;
@property (nonatomic, strong) RestoMenuItem *soup;
@property (nonatomic, strong) NSDate *lastUpdated;

+ (RKObjectMapping *)objectMapping;

@end

@interface RestoMenuItem : NSObject <NSCoding>

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) NSString *price;
@property (nonatomic) BOOL recommended;

@end

//
//  MPAdapterMap.m
//  MoPub
//
//  Created by Andrew He on 1/26/11.
//  Copyright 2011 MoPub, Inc. All rights reserved.
//

#import "MPAdapterMap.h"
#import "MPLogging.h"

static MPAdapterMap *sharedAdapterMap = nil;

@implementation MPAdapterMap

+ (id)sharedAdapterMap
{
	@synchronized(self)
	{
		if (sharedAdapterMap == nil)
			sharedAdapterMap = [[self alloc] init];
	}
	return sharedAdapterMap;
}

- (id)init
{
	if (self = [super init])
	{
		_map = [[NSDictionary dictionaryWithObjectsAndKeys:
				 @"MPIAdAdapter", @"iAd",
				 @"MPGoogleAdSenseAdapter", @"adsense",
				 @"MPCustomEventAdapter", @"custom",
				 nil] retain];
	}
	return self;
}

- (void)dealloc
{
	[_map release];
	[super dealloc];
}

- (NSString *)classStringForAdapterType:(NSString *)type
{
	return [_map objectForKey:type];
}

- (Class)classForAdapterType:(NSString *)type
{
	return NSClassFromString([_map objectForKey:type]);
}

@end

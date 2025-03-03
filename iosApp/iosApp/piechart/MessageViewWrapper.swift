//
//  MessageViewWrapper.swift
//  iosApp
//
//  Created by Murali Vipparla on 7/30/24.
//  Copyright © 2024 orgName. All rights reserved.
//

import SwiftUI
import UIKit

@objc
public class MessageViewWrapper: NSObject {
    @objc
    public static func makeMessageViewControllerWithMessage(_ message: NSString) -> UIViewController {
        return UIHostingController(rootView: MessageView(message: message as String))
    }
}

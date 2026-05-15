const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  Header, Footer, AlignmentType, HeadingLevel, BorderStyle, WidthType,
  ShadingType, VerticalAlign, PageBreak, LevelFormat,
  TabStopType, TabStopPosition, ExternalHyperlink
} = require('docx');
const fs = require('fs');

const GREEN = "1D6B45";
const LIGHT_GREEN = "E8F5EE";
const DARK = "1A1A1A";
const GRAY = "555555";
const LIGHT_GRAY = "F5F5F5";
const MID_GRAY = "CCCCCC";
const WHITE = "FFFFFF";
const AMBER = "BA7517";
const LIGHT_AMBER = "FFF8E7";
const BLUE = "1A5F9E";
const LIGHT_BLUE = "EBF3FB";
const RED = "A32D2D";
const LIGHT_RED = "FBEAEA";
const PURPLE = "5B2D8E";
const LIGHT_PURPLE = "F3EEFB";

const border = (color = MID_GRAY) => ({ style: BorderStyle.SINGLE, size: 1, color });
const borders = (color = MID_GRAY) => ({ top: border(color), bottom: border(color), left: border(color), right: border(color) });
const noBorder = () => ({ style: BorderStyle.NONE, size: 0, color: "FFFFFF" });
const noBorders = () => ({ top: noBorder(), bottom: noBorder(), left: noBorder(), right: noBorder() });

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 360, after: 160 },
    children: [new TextRun({ text, bold: true, size: 36, color: DARK, font: "Arial" })]
  });
}
function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 280, after: 120 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: GREEN, space: 4 } },
    children: [new TextRun({ text, bold: true, size: 28, color: GREEN, font: "Arial" })]
  });
}
function h2purple(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 280, after: 120 },
    border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: PURPLE, space: 4 } },
    children: [new TextRun({ text, bold: true, size: 28, color: PURPLE, font: "Arial" })]
  });
}
function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 200, after: 80 },
    children: [new TextRun({ text, bold: true, size: 24, color: DARK, font: "Arial" })]
  });
}
function h4(text) {
  return new Paragraph({
    spacing: { before: 160, after: 60 },
    children: [new TextRun({ text, bold: true, size: 22, color: GRAY, font: "Arial" })]
  });
}
function para(text, opts = {}) {
  return new Paragraph({
    spacing: { before: 60, after: 100 },
    children: [new TextRun({ text, size: 22, color: DARK, font: "Arial", ...opts })]
  });
}
function code(text) {
  return new Paragraph({
    spacing: { before: 40, after: 40 },
    shading: { fill: "1E1E1E", type: ShadingType.CLEAR },
    border: { left: { style: BorderStyle.SINGLE, size: 12, color: GREEN, space: 6 } },
    indent: { left: 360 },
    children: [new TextRun({ text, size: 18, color: "D4D4D4", font: "Courier New" })]
  });
}
function codeBlock(lines) { return lines.map(l => code(l)); }
function bullet(text, level = 0) {
  return new Paragraph({
    numbering: { reference: "bullets", level },
    spacing: { before: 40, after: 40 },
    children: [new TextRun({ text, size: 22, color: DARK, font: "Arial" })]
  });
}
function numbered(text, level = 0) {
  return new Paragraph({
    numbering: { reference: "numbers", level },
    spacing: { before: 40, after: 40 },
    children: [new TextRun({ text, size: 22, color: DARK, font: "Arial" })]
  });
}
function spacer(n = 1) { return Array.from({ length: n }, () => new Paragraph({ children: [new TextRun("")] })); }
function colorBox(text, fillColor, textColor = DARK) {
  return new Paragraph({
    spacing: { before: 120, after: 120 },
    shading: { fill: fillColor, type: ShadingType.CLEAR },
    border: { left: { style: BorderStyle.SINGLE, size: 16, color: GREEN, space: 6 } },
    indent: { left: 200, right: 200 },
    children: [new TextRun({ text, size: 22, color: textColor, bold: true, font: "Arial" })]
  });
}
function purpleBox(text) {
  return new Paragraph({
    spacing: { before: 120, after: 120 },
    shading: { fill: LIGHT_PURPLE, type: ShadingType.CLEAR },
    border: { left: { style: BorderStyle.SINGLE, size: 16, color: PURPLE, space: 6 } },
    indent: { left: 200, right: 200 },
    children: [new TextRun({ text, size: 22, color: PURPLE, bold: true, font: "Arial" })]
  });
}
function phaseTable(rows) {
  const totalWidth = 9360;
  const col1 = 2200, col2 = 4360, col3 = 1400, col4 = 1400;
  return new Table({
    width: { size: totalWidth, type: WidthType.DXA },
    columnWidths: [col1, col2, col3, col4],
    rows: rows.map((r, i) => new TableRow({
      children: r.map((cell, j) => {
        const isHeader = i === 0;
        const widths = [col1, col2, col3, col4];
        return new TableCell({
          borders: borders(isHeader ? GREEN : MID_GRAY),
          width: { size: widths[j], type: WidthType.DXA },
          shading: { fill: isHeader ? GREEN : (i % 2 === 0 ? LIGHT_GRAY : WHITE), type: ShadingType.CLEAR },
          margins: { top: 80, bottom: 80, left: 120, right: 120 },
          children: [new Paragraph({
            children: [new TextRun({ text: cell, size: isHeader ? 20 : 19, bold: isHeader, color: isHeader ? WHITE : DARK, font: "Arial" })]
          })]
        });
      })
    }))
  });
}
function simpleTable(headers, rows, colWidths) {
  const totalWidth = 9360;
  const cols = colWidths || Array(headers.length).fill(Math.floor(totalWidth / headers.length));
  const allRows = [headers, ...rows];
  return new Table({
    width: { size: totalWidth, type: WidthType.DXA },
    columnWidths: cols,
    rows: allRows.map((r, i) => new TableRow({
      children: r.map((cell, j) => {
        const isHeader = i === 0;
        return new TableCell({
          borders: borders(isHeader ? GREEN : MID_GRAY),
          width: { size: cols[j], type: WidthType.DXA },
          shading: { fill: isHeader ? GREEN : (i % 2 === 0 ? LIGHT_GRAY : WHITE), type: ShadingType.CLEAR },
          margins: { top: 80, bottom: 80, left: 120, right: 120 },
          children: [new Paragraph({
            children: [new TextRun({ text: String(cell), size: isHeader ? 20 : 19, bold: isHeader, color: isHeader ? WHITE : DARK, font: "Arial" })]
          })]
        });
      })
    }))
  });
}
function adminTable(headers, rows, colWidths) {
  const totalWidth = 9360;
  const cols = colWidths || Array(headers.length).fill(Math.floor(totalWidth / headers.length));
  const allRows = [headers, ...rows];
  return new Table({
    width: { size: totalWidth, type: WidthType.DXA },
    columnWidths: cols,
    rows: allRows.map((r, i) => new TableRow({
      children: r.map((cell, j) => {
        const isHeader = i === 0;
        return new TableCell({
          borders: borders(isHeader ? PURPLE : MID_GRAY),
          width: { size: cols[j], type: WidthType.DXA },
          shading: { fill: isHeader ? PURPLE : (i % 2 === 0 ? LIGHT_PURPLE : WHITE), type: ShadingType.CLEAR },
          margins: { top: 80, bottom: 80, left: 120, right: 120 },
          children: [new Paragraph({
            children: [new TextRun({ text: String(cell), size: isHeader ? 20 : 19, bold: isHeader, color: isHeader ? WHITE : DARK, font: "Arial" })]
          })]
        });
      })
    }))
  });
}

const doc = new Document({
  numbering: {
    config: [
      {
        reference: "bullets",
        levels: [
          { level: 0, format: LevelFormat.BULLET, text: "\u2022", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } },
          { level: 1, format: LevelFormat.BULLET, text: "\u25E6", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 1080, hanging: 360 } } } }
        ]
      },
      {
        reference: "numbers",
        levels: [
          { level: 0, format: LevelFormat.DECIMAL, text: "%1.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 720, hanging: 360 } } } },
          { level: 1, format: LevelFormat.DECIMAL, text: "%1.%2.", alignment: AlignmentType.LEFT, style: { paragraph: { indent: { left: 1080, hanging: 360 } } } }
        ]
      }
    ]
  },
  styles: {
    default: { document: { run: { font: "Arial", size: 22 } } },
    paragraphStyles: [
      { id: "Heading1", name: "Heading 1", basedOn: "Normal", next: "Normal", quickFormat: true, run: { size: 36, bold: true, font: "Arial", color: DARK }, paragraph: { spacing: { before: 360, after: 160 }, outlineLevel: 0 } },
      { id: "Heading2", name: "Heading 2", basedOn: "Normal", next: "Normal", quickFormat: true, run: { size: 28, bold: true, font: "Arial", color: GREEN }, paragraph: { spacing: { before: 280, after: 120 }, outlineLevel: 1 } },
      { id: "Heading3", name: "Heading 3", basedOn: "Normal", next: "Normal", quickFormat: true, run: { size: 24, bold: true, font: "Arial", color: DARK }, paragraph: { spacing: { before: 200, after: 80 }, outlineLevel: 2 } },
    ]
  },
  sections: [{
    properties: {
      page: { size: { width: 12240, height: 15840 }, margin: { top: 1440, right: 1440, bottom: 1440, left: 1440 } }
    },
    headers: {
      default: new Header({
        children: [
          new Paragraph({
            border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: GREEN, space: 6 } },
            spacing: { after: 120 },
            children: [
              new TextRun({ text: "Koshai Finder Bangladesh  —  Complete Development Plan (3 Roles)", size: 18, color: GRAY, font: "Arial" }),
              new TextRun({ text: "\t", size: 18 }),
              new TextRun({ text: "Confidential", size: 18, color: GREEN, font: "Arial", bold: true })
            ],
            tabStops: [{ type: TabStopType.RIGHT, position: TabStopPosition.MAX }]
          })
        ]
      })
    },
    footers: {
      default: new Footer({
        children: [
          new Paragraph({
            border: { top: { style: BorderStyle.SINGLE, size: 4, color: GREEN, space: 6 } },
            spacing: { before: 120 },
            children: [
              new TextRun({ text: "Koshai Finder  |  Android (Kotlin) + Firebase  |  3 Roles: User, Koshai, Admin  |  Bangladesh", size: 16, color: GRAY, font: "Arial" })
            ]
          })
        ]
      })
    },
    children: [

      // ── COVER ────────────────────────────────────────────────────────────
      new Paragraph({ spacing: { before: 1440, after: 240 }, shading: { fill: GREEN, type: ShadingType.CLEAR }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: " ", size: 4 })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 0, after: 0 }, shading: { fill: GREEN, type: ShadingType.CLEAR }, children: [new TextRun({ text: "Koshai Finder", size: 72, bold: true, color: WHITE, font: "Arial" })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 80, after: 0 }, shading: { fill: GREEN, type: ShadingType.CLEAR }, children: [new TextRun({ text: "কোশাই ফাইন্ডার বাংলাদেশ", size: 36, color: "C8F0DC", font: "Arial" })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 120, after: 80 }, shading: { fill: GREEN, type: ShadingType.CLEAR }, children: [new TextRun({ text: "Complete Product, Technical & Pipeline Document", size: 26, color: "A8DCC0", font: "Arial" })] }),
      new Paragraph({ spacing: { before: 0, after: 1440 }, shading: { fill: GREEN, type: ShadingType.CLEAR }, alignment: AlignmentType.CENTER, children: [new TextRun({ text: " ", size: 4 })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 200, after: 80 }, children: [new TextRun({ text: "Android Studio  |  Kotlin  |  Firebase  |  Google Maps SDK", size: 22, color: GRAY, font: "Arial" })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 0, after: 80 }, children: [new TextRun({ text: "Target Market: Bangladesh  |  Peak Use Case: Eid ul-Adha", size: 22, color: GRAY, font: "Arial" })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 0, after: 80 }, children: [new TextRun({ text: "Roles: User  |  Koshai  |  Admin", size: 22, color: PURPLE, bold: true, font: "Arial" })] }),
      new Paragraph({ alignment: AlignmentType.CENTER, spacing: { before: 0, after: 80 }, children: [new TextRun({ text: "Total Features: 22  |  Phases: 4  |  Estimated Timeline: 24 Weeks", size: 22, color: GRAY, font: "Arial" })] }),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 1. EXECUTIVE SUMMARY ─────────────────────────────────────────────
      h1("1. Executive Summary"),
      para("During Eid ul-Adha, millions of Bangladeshi families sacrifice animals (cows, goats, sheep) as part of religious observance. Finding a skilled and available koshai (animal slaughterer) becomes extremely difficult — demand spikes in a window of just 24–48 hours, communication is entirely word-of-mouth, and no-shows are common. Families waste hours searching, and skilled koshais lose potential income due to poor scheduling."),
      para("Koshai Finder solves this with a three-sided marketplace app: Users find, book, and pay verified koshais near them; Koshais manage their schedule, availability, and earnings; and Admins oversee the entire platform — verifying koshais, resolving disputes, managing fraud alerts, and controlling platform-wide settings. Built on Android (Kotlin) with Firebase as the backend, the app targets the 170+ million population of Bangladesh."),
      ...spacer(1),
      purpleBox("NEW: Admin Role — A dedicated super-user role with a web-based dashboard and in-app panel to manage koshai verification, user disputes, fraud flags, surge settings, and platform analytics."),
      ...spacer(1),
      simpleTable(
        ["Metric", "Value"],
        [
          ["Platform", "Android (minSDK 21 — covers 98% of Bangladeshi devices) + Admin Web App"],
          ["Language", "Kotlin (Android) + Node.js/TypeScript (Cloud Functions) + React (Admin Dashboard)"],
          ["Backend", "Firebase (Auth, Firestore, Realtime DB, FCM, Storage, Cloud Functions)"],
          ["Maps", "Google Maps SDK + Google Places API"],
          ["Payment", "bKash & Nagad payment gateway (Phase 3)"],
          ["Total roles", "3 — User, Koshai, Admin"],
          ["Total screens", "~32 screens (user: 12, koshai: 10, admin mobile: 5, admin web: 5+)"],
          ["Total features", "22 features across 4 release phases"],
          ["Timeline", "24 weeks (6 weeks per phase)"],
          ["Primary language", "Bangla (বাংলা) app UI + English admin panel"],
        ],
        [3000, 6360]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 2. ROLES ─────────────────────────────────────────────────────────
      h1("2. Three-Role System Overview"),
      para("The app supports three distinct roles, each with its own authentication path, screens, and Firestore permissions. A single Firebase Auth account is tied to exactly one role — stored in the users, koshais, or admins collections respectively."),
      ...spacer(1),
      simpleTable(
        ["Role", "Who", "Access Level", "Auth Routing"],
        [
          ["User", "Families / individuals who need a koshai", "Book, track, review, pay, chat", "Phone OTP → role: user"],
          ["Koshai", "Registered animal slaughterers", "Accept bookings, manage schedule, earn, chat", "Phone OTP → role: koshai → pending approval"],
          ["Admin", "Platform operators / company staff", "Verify koshais, resolve disputes, manage platform", "Email + Password → role: admin (invite-only)"],
        ],
        [1400, 2400, 3160, 2400]
      ),
      ...spacer(1),
      para("Role is determined immediately after OTP verification. A Firestore lookup checks which collection the UID belongs to. Admins use email/password auth (not OTP) and log in through a separate entry point — either the admin web dashboard or the in-app Admin Panel (restricted to admin UIDs only)."),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 3. PROBLEM & SOLUTION ─────────────────────────────────────────────
      h1("3. Problem Statement & Solution"),
      h2("3.1 The Problem"),
      bullet("Demand for koshais spikes to 10–50x normal in a 24-hour window during Eid ul-Adha"),
      bullet("No centralized platform to find, verify, or book koshais in Bangladesh"),
      bullet("Word-of-mouth only — families rely on neighbors or mosque connections"),
      bullet("Double bookings and no-shows are widespread, leaving families stranded on Eid morning"),
      bullet("Koshais cannot manage multiple bookings efficiently — no scheduling tools"),
      bullet("No price transparency — users often overpay or get quoted unfair rates last minute"),
      bullet("No trust mechanism — users cannot verify a koshai's skill, hygiene, or legitimacy"),
      bullet("Platform operators have no tools to verify koshais, detect fraud, or resolve disputes"),
      ...spacer(1),
      h2("3.2 The Solution"),
      bullet("Three-sided marketplace: users book, koshais accept, admins oversee — all inside one system"),
      bullet("Slot-based scheduling prevents double booking and manages Eid-morning rush"),
      bullet("Admin-controlled verified badge system (NID + trade certificate review)"),
      bullet("In-app payment with bKash/Nagad eliminates cash ambiguity and reduces no-shows"),
      bullet("Ratings and reviews create accountability and surface top performers"),
      bullet("Real-time availability and live tracking give users confidence on the day"),
      bullet("Admin dashboard provides fraud detection, dispute resolution, and analytics"),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 4. TECH STACK ─────────────────────────────────────────────────────
      h1("4. Full Technology Stack"),
      simpleTable(
        ["Layer", "Technology", "Purpose"],
        [
          ["IDE", "Android Studio Hedgehog+", "Project scaffolding, debugging, emulator"],
          ["Language", "Kotlin 1.9+", "All Android app code"],
          ["UI framework", "Material Design 3 + ViewBinding", "Screens, components, navigation"],
          ["Navigation", "Jetpack Navigation Component", "Fragment-based screen routing"],
          ["Architecture", "MVVM + Repository pattern", "Separation of concerns, testability"],
          ["Auth (User/Koshai)", "Firebase Authentication (Phone OTP)", "Login for users and koshais"],
          ["Auth (Admin)", "Firebase Authentication (Email/Password)", "Invite-only admin login"],
          ["Database", "Cloud Firestore", "Users, koshais, bookings, reviews, admins"],
          ["Realtime", "Firebase Realtime Database", "Live location, booking status, chat"],
          ["Push alerts", "Firebase Cloud Messaging (FCM)", "Booking confirmations, reminders"],
          ["File storage", "Firebase Storage", "Profile photos, NID scans, review images"],
          ["Backend logic", "Firebase Cloud Functions (Node.js)", "Notifications, fraud detection, admin triggers"],
          ["Maps", "Google Maps SDK for Android", "Koshai pins, live tracking, heatmap"],
          ["Location", "Google Play Services Location API", "User + koshai GPS coordinates"],
          ["Payment", "bKash PGW API + Nagad API", "Booking deposits, fee collection"],
          ["Admin web app", "React + Firebase Hosting", "Koshai verification, analytics, ban management"],
          ["Image loading", "Glide 4.x", "Profile photos, review images"],
          ["HTTP", "Retrofit 2 + OkHttp", "bKash/Nagad API calls"],
          ["DI", "Hilt (Dagger)", "Dependency injection"],
          ["Async", "Kotlin Coroutines + Flow", "Background tasks, reactive UI"],
          ["Testing", "JUnit 4 + Espresso + MockK", "Unit and UI tests"],
          ["CI/CD", "GitHub Actions", "Automated APK builds + Play Store upload"],
        ],
        [2400, 3000, 3960]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 5. FIRESTORE SCHEMA ──────────────────────────────────────────────
      h1("5. Complete Firebase Data Architecture"),
      h2("5.1 Firestore Collections Schema"),

      h3("Collection: users"),
      ...codeBlock([
        "users/{uid}",
        "{",
        "  uid: String,               // Firebase Auth UID",
        "  name: String,              // Full name",
        "  phone: String,             // +8801XXXXXXXXX",
        "  photoUrl: String,          // Firebase Storage URL",
        "  district: String,          // e.g. Narayanganj",
        "  upazila: String,           // Sub-district",
        "  fcmToken: String,          // For push notifications",
        "  createdAt: Timestamp,",
        "  referralCode: String,      // Unique invite code",
        "  referredBy: String,        // UID of referrer",
        "  credits: Number            // Discount credits in BDT",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: koshais"),
      ...codeBlock([
        "koshais/{uid}",
        "{",
        "  uid: String,               // Firebase Auth UID",
        "  name: String,",
        "  phone: String,",
        "  photoUrl: String,",
        "  nidUrl: String,            // Firebase Storage: NID scan",
        "  tradeCertUrl: String,      // Firebase Storage: certificate (optional)",
        "  district: String,          // Primary coverage district",
        "  upazila: String,",
        "  coverageAreas: [String],   // List of additional upazilas covered",
        "  ratePerCow: Number,        // BDT",
        "  ratePerGoat: Number,       // BDT",
        "  ratePerSheep: Number,      // BDT",
        "  surgeMultiplier: Number,   // Default 1.0; up to 2.0 during Eid",
        "  status: String,            // available | busy | offline",
        "  isVerified: Boolean,       // Set by admin only",
        "  isEidMode: Boolean,        // Surge pricing active",
        "  isFlagged: Boolean,        // Fraud/abuse flag set by admin",
        "  isBanned: Boolean,         // Permanent ban set by admin",
        "  banReason: String,         // Reason recorded by admin",
        "  rating: Number,            // Running average (0.0–5.0)",
        "  totalRatings: Number,      // Count of ratings received",
        "  totalJobs: Number,         // Completed bookings",
        "  lat: Number,               // Last known latitude",
        "  lng: Number,               // Last known longitude",
        "  locationUpdatedAt: Timestamp,",
        "  fcmToken: String,",
        "  earnings: Number,          // Lifetime BDT received",
        "  createdAt: Timestamp,",
        "  approvedAt: Timestamp,     // When admin verified",
        "  approvedBy: String         // Admin UID who approved",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: admins  [NEW]"),
      ...codeBlock([
        "admins/{uid}",
        "{",
        "  uid: String,               // Firebase Auth UID (email/password)",
        "  name: String,              // Admin staff name",
        "  email: String,             // Login email",
        "  role: String,              // superadmin | moderator | support",
        "  permissions: {",
        "    canVerifyKoshais: Boolean,",
        "    canBanUsers: Boolean,",
        "    canResolveDIsputes: Boolean,",
        "    canEditSurge: Boolean,",
        "    canViewAnalytics: Boolean,",
        "    canManageAdmins: Boolean  // superadmin only",
        "  },",
        "  createdAt: Timestamp,",
        "  createdBy: String,         // UID of admin who created this account",
        "  lastLoginAt: Timestamp,",
        "  isActive: Boolean          // Revoke access without deleting account",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: adminAlerts  [NEW]"),
      ...codeBlock([
        "adminAlerts/{alertId}",
        "{",
        "  type: String,              // fraud_suspected | dispute | nid_pending | abuse_report",
        "  koshaiId: String,          // Related koshai (if applicable)",
        "  userId: String,            // Related user (if applicable)",
        "  bookingId: String,         // Related booking (if applicable)",
        "  message: String,           // Human-readable alert description",
        "  severity: String,          // low | medium | high | critical",
        "  status: String,            // open | in_review | resolved | dismissed",
        "  resolvedBy: String,        // Admin UID who acted",
        "  resolvedAt: Timestamp,",
        "  resolution: String,        // Admin's note on resolution",
        "  createdAt: Timestamp",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: disputes  [NEW]"),
      ...codeBlock([
        "disputes/{disputeId}",
        "{",
        "  bookingId: String,",
        "  raisedBy: String,          // user | koshai",
        "  raisedByUid: String,",
        "  reason: String,            // no_show | wrong_rate | damage | other",
        "  description: String,       // Free text",
        "  evidenceUrls: [String],    // Uploaded photos/docs",
        "  status: String,            // open | under_review | resolved | escalated",
        "  assignedAdmin: String,     // Admin UID handling this dispute",
        "  resolution: String,        // Admin decision text",
        "  refundAmount: Number,      // BDT refunded if applicable",
        "  createdAt: Timestamp,",
        "  resolvedAt: Timestamp",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: platformSettings  [NEW]"),
      ...codeBlock([
        "platformSettings/global",
        "{",
        "  eidModeActive: Boolean,         // Global Eid mode toggle",
        "  eidStartDate: Timestamp,",
        "  eidEndDate: Timestamp,",
        "  defaultSurgeMultiplier: Number, // Applied to all koshais in Eid mode",
        "  maxSurgeMultiplier: Number,     // Cap at this value",
        "  bookingDepositPercent: Number,  // e.g. 10 = 10% deposit required",
        "  platformCommissionPercent: Number,  // e.g. 5",
        "  maintenanceMode: Boolean,       // Disables booking if true",
        "  maintenanceMessage: String,     // Shown to users during maintenance",
        "  updatedBy: String,              // Admin UID",
        "  updatedAt: Timestamp",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: bookings"),
      ...codeBlock([
        "bookings/{bookingId}",
        "{",
        "  id: String,                // Auto-generated Firestore ID",
        "  userId: String,",
        "  userName: String,",
        "  userPhone: String,",
        "  koshaiId: String,",
        "  koshaiName: String,",
        "  koshaiPhone: String,",
        "  district: String,",
        "  upazila: String,",
        "  address: String,",
        "  lat: Number,",
        "  lng: Number,",
        "  date: Timestamp,",
        "  slot: String,              // e.g. 07:00-09:00",
        "  animalTypes: { cow: Number, goat: Number, sheep: Number },",
        "  estimatedDurationMins: Number,",
        "  rateBreakdown: {",
        "    perCow: Number, perGoat: Number, perSheep: Number,",
        "    surgeMultiplier: Number, total: Number",
        "  },",
        "  depositPaid: Number,",
        "  paymentRef: String,",
        "  status: String,            // pending|confirmed|en_route|arrived|completed|cancelled",
        "  cancelReason: String,",
        "  cancelledBy: String,       // user | koshai | admin",
        "  isGroupBooking: Boolean,",
        "  groupSessionId: String,",
        "  isTeamBooking: Boolean,",
        "  teamKoshaiIds: [String],",
        "  createdAt: Timestamp,",
        "  confirmedAt: Timestamp,",
        "  completedAt: Timestamp,",
        "  adminNote: String,         // Note added by admin if intervened",
        "  reminder24Sent: Boolean,",
        "  reminder2Sent: Boolean",
        "}",
      ]),
      ...spacer(1),

      h3("Collection: reviews"),
      ...codeBlock([
        "reviews/{reviewId}",
        "{",
        "  bookingId: String,",
        "  userId: String,",
        "  koshaiId: String,",
        "  rating: Number,            // 1–5",
        "  comment: String,",
        "  photoUrls: [String],",
        "  isHidden: Boolean,         // Admin can hide abusive reviews",
        "  hiddenBy: String,          // Admin UID",
        "  createdAt: Timestamp",
        "}",
      ]),
      ...spacer(1),

      h3("Realtime Database — live locations & booking status"),
      ...codeBlock([
        "realtime_db/",
        "  locations/{koshaiId}/",
        "    lat: Double",
        "    lng: Double",
        "    updatedAt: Long",
        "  bookingStatus/{bookingId}: String",
        "  adminOnline/{adminUid}: Boolean    // For admin activity indicator",
      ]),

      h2("5.2 Firestore Security Rules (Updated for Admin Role)"),
      ...codeBlock([
        "rules_version = '2';",
        "service cloud.firestore {",
        "  match /databases/{database}/documents {",
        "",
        "    // Helper: check if requester is an active admin",
        "    function isAdmin() {",
        "      return exists(/databases/$(database)/documents/admins/$(request.auth.uid))",
        "             && get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.isActive == true;",
        "    }",
        "",
        "    // Admins collection: only superadmins can write",
        "    match /admins/{uid} {",
        "      allow read: if isAdmin();",
        "      allow write: if isAdmin()",
        "                   && get(/databases/$(database)/documents/admins/$(request.auth.uid)).data.role == 'superadmin';",
        "    }",
        "",
        "    // Users: read own, write own; admin can read all",
        "    match /users/{uid} {",
        "      allow read: if request.auth.uid == uid || isAdmin();",
        "      allow write: if request.auth.uid == uid;",
        "    }",
        "",
        "    // Koshais: anyone authenticated can read; owner writes most fields;",
        "    //          admin can write isVerified, isBanned, isFlagged, approvedBy",
        "    match /koshais/{uid} {",
        "      allow read: if request.auth != null;",
        "      allow update: if (request.auth.uid == uid",
        "                       && !request.resource.data.keys().hasAny(['isVerified','isBanned','approvedBy']))",
        "                    || isAdmin();",
        "      allow create: if request.auth.uid == uid;",
        "    }",
        "",
        "    // Bookings: user or koshai of that booking; admin can read/write all",
        "    match /bookings/{bookingId} {",
        "      allow read, write: if request.auth.uid == resource.data.userId",
        "                         || request.auth.uid == resource.data.koshaiId",
        "                         || isAdmin();",
        "      allow create: if request.auth != null;",
        "    }",
        "",
        "    // Reviews: any auth user can read (unless hidden); user creates; admin can hide",
        "    match /reviews/{reviewId} {",
        "      allow read: if request.auth != null && (resource.data.isHidden == false || isAdmin());",
        "      allow create: if request.auth.uid == request.resource.data.userId;",
        "      allow update: if isAdmin();",
        "    }",
        "",
        "    // Disputes: raiser or admin",
        "    match /disputes/{disputeId} {",
        "      allow read, write: if request.auth.uid == resource.data.raisedByUid || isAdmin();",
        "      allow create: if request.auth != null;",
        "    }",
        "",
        "    // Admin alerts: admin only",
        "    match /adminAlerts/{alertId} {",
        "      allow read, write: if isAdmin();",
        "    }",
        "",
        "    // Platform settings: admin only",
        "    match /platformSettings/{doc} {",
        "      allow read: if request.auth != null;",
        "      allow write: if isAdmin();",
        "    }",
        "  }",
        "}",
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 6. SCREENS ───────────────────────────────────────────────────────
      h1("6. Complete Screen & Navigation Plan"),
      h2("6.1 User Screens (12 screens)"),
      simpleTable(
        ["Screen", "Fragment/Activity", "Description"],
        [
          ["Splash", "SplashActivity", "Logo, check auth state, route to Auth or Home"],
          ["Role select", "RoleSelectFragment", "Are you a User or a Koshai? Two big buttons (Admin logs in via web)"],
          ["Phone input", "PhoneInputFragment", "Enter +88 number, send OTP"],
          ["OTP verify", "OtpVerifyFragment", "6-digit OTP input, resend timer"],
          ["Home (map)", "HomeFragment", "Google Map with koshai pins, search bar, filter FAB"],
          ["Search/Filter", "SearchFilterFragment", "Filter by district, upazila, animal, price, availability"],
          ["Koshai profile", "KoshaiProfileFragment", "Photo, rating, rates, slots, reviews, Book button"],
          ["Booking form", "BookingFormFragment", "Date, slot, address, animal count, price estimate"],
          ["Payment", "PaymentFragment", "bKash/Nagad SDK, deposit confirmation"],
          ["Live tracker", "BookingTrackerFragment", "Real-time status bar + koshai location on mini-map"],
          ["My bookings", "MyBookingsFragment", "List of all bookings with status badges"],
          ["Write review", "WriteReviewFragment", "Stars, comment, optional photo upload"],
        ],
        [2200, 2800, 4360]
      ),
      ...spacer(1),
      h2("6.2 Koshai Screens (10 screens)"),
      simpleTable(
        ["Screen", "Fragment/Activity", "Description"],
        [
          ["Registration", "KoshaiRegisterFragment", "Name, NID upload, photo, district, rates per animal"],
          ["Pending approval", "PendingApprovalFragment", "Show waiting state until admin verifies NID"],
          ["Dashboard", "KoshaiDashboardFragment", "Incoming requests count, today's schedule, toggle status"],
          ["Availability toggle", "AvailabilityFragment", "Open / Busy / Offline switch, Eid mode & surge rate"],
          ["Booking manager", "BookingManagerFragment", "List of pending/confirmed bookings, Accept/Decline"],
          ["Booking detail", "BookingDetailFragment", "Full booking info, chat button, complete/cancel action"],
          ["In-app chat", "ChatFragment", "Message thread for a specific booking"],
          ["Earnings", "EarningsFragment", "Total earned, per-booking history, monthly breakdown"],
          ["Leaderboard", "LeaderboardFragment", "District ranking, own position highlight"],
          ["My profile", "KoshaiProfileEditFragment", "Edit rates, coverage area, photo, toggle Eid mode"],
        ],
        [2200, 2800, 4360]
      ),
      ...spacer(1),
      h2purple("6.3 Admin Screens — Mobile In-App Panel (5 screens)  [NEW]"),
      para("Admin users who log in on Android are routed to a restricted Admin Panel (not visible to regular users or koshais). This provides quick actions on-the-go. The full dashboard is the web app."),
      adminTable(
        ["Screen", "Fragment/Activity", "Description"],
        [
          ["Admin login", "AdminLoginFragment", "Email + password auth (separate entry, not OTP). Checks admins/{uid} for isActive."],
          ["Admin dashboard", "AdminDashboardFragment", "Overview cards: pending verifications, open alerts, active bookings, platform status toggle"],
          ["Koshai verification queue", "VerificationQueueFragment", "List of koshais with isVerified=false; tap to view NID, approve or reject with reason"],
          ["Alert center", "AlertCenterFragment", "Live feed of adminAlerts sorted by severity; tap to view details and mark resolved"],
          ["Dispute manager", "DisputeManagerFragment", "Open disputes list; assign to self, add resolution note, trigger refund"],
        ],
        [2200, 2800, 4360]
      ),
      ...spacer(1),
      h2purple("6.4 Admin Web Dashboard (5 pages)  [NEW]"),
      para("Hosted on Firebase Hosting at admin.koshaiapp.com. Built with React + Firebase SDK. Admin must be authenticated and present in the admins collection with isActive=true."),
      adminTable(
        ["Page", "Route", "Description"],
        [
          ["Overview", "/dashboard", "KPI cards: daily bookings, revenue, new koshais, active users, open disputes. Line charts for trends."],
          ["Koshai management", "/koshais", "Full table of all koshais with filters. Approve/reject NID, ban/unban, edit surge cap, view earnings."],
          ["Booking oversight", "/bookings", "All bookings with status filter. Can cancel on behalf of user/koshai, add admin note."],
          ["Disputes & alerts", "/disputes", "Unified view of disputes and fraud alerts. Assign, resolve, dismiss with audit trail."],
          ["Platform settings", "/settings", "Toggle Eid mode, set surge limits, booking deposit %, platform commission, maintenance mode banner."],
        ],
        [1600, 1800, 5960]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 7. KOTLIN CODE ───────────────────────────────────────────────────
      h1("7. Key Kotlin Implementation Code"),

      h2("7.1 Gradle Dependencies (app/build.gradle)"),
      ...codeBlock([
        "dependencies {",
        "  implementation(platform('com.google.firebase:firebase-bom:32.7.0'))",
        "  implementation('com.google.firebase:firebase-auth-ktx')",
        "  implementation('com.google.firebase:firebase-firestore-ktx')",
        "  implementation('com.google.firebase:firebase-database-ktx')",
        "  implementation('com.google.firebase:firebase-messaging-ktx')",
        "  implementation('com.google.firebase:firebase-storage-ktx')",
        "  implementation('com.google.firebase:firebase-functions-ktx')",
        "  implementation('com.google.android.gms:play-services-maps:18.2.0')",
        "  implementation('com.google.android.gms:play-services-location:21.0.1')",
        "  implementation('androidx.navigation:navigation-fragment-ktx:2.7.6')",
        "  implementation('androidx.navigation:navigation-ui-ktx:2.7.6')",
        "  implementation('androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0')",
        "  implementation('androidx.lifecycle:lifecycle-runtime-ktx:2.7.0')",
        "  implementation('androidx.hilt:hilt-navigation-fragment:1.1.0')",
        "  implementation('com.google.dagger:hilt-android:2.50')",
        "  kapt('com.google.dagger:hilt-compiler:2.50')",
        "  implementation('com.squareup.retrofit2:retrofit:2.9.0')",
        "  implementation('com.squareup.retrofit2:converter-gson:2.9.0')",
        "  implementation('com.squareup.okhttp3:logging-interceptor:4.12.0')",
        "  implementation('com.github.bumptech.glide:glide:4.16.0')",
        "  implementation('com.google.android.material:material:1.11.0')",
        "  implementation('org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3')",
        "  implementation('org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3')",
        "}",
      ]),
      ...spacer(1),

      h2("7.2 Data Models (Kotlin Data Classes)"),
      ...codeBlock([
        "// Koshai.kt",
        "data class Koshai(",
        "  val uid: String = \"\",",
        "  val name: String = \"\",",
        "  val phone: String = \"\",",
        "  val photoUrl: String = \"\",",
        "  val district: String = \"\",",
        "  val upazila: String = \"\",",
        "  val ratePerCow: Int = 0,",
        "  val ratePerGoat: Int = 0,",
        "  val status: String = \"offline\",",
        "  val isVerified: Boolean = false,",
        "  val isFlagged: Boolean = false,",
        "  val isBanned: Boolean = false,",
        "  val rating: Double = 0.0,",
        "  val totalJobs: Int = 0,",
        "  val lat: Double = 0.0,",
        "  val lng: Double = 0.0,",
        "  val surgeMultiplier: Double = 1.0,",
        "  val isEidMode: Boolean = false",
        ")",
        "",
        "// Admin.kt",
        "data class Admin(",
        "  val uid: String = \"\",",
        "  val name: String = \"\",",
        "  val email: String = \"\",",
        "  val role: String = \"moderator\",  // superadmin | moderator | support",
        "  val permissions: AdminPermissions = AdminPermissions(),",
        "  val isActive: Boolean = true,",
        "  val lastLoginAt: com.google.firebase.Timestamp? = null",
        ")",
        "",
        "data class AdminPermissions(",
        "  val canVerifyKoshais: Boolean = false,",
        "  val canBanUsers: Boolean = false,",
        "  val canResolveDisputes: Boolean = false,",
        "  val canEditSurge: Boolean = false,",
        "  val canViewAnalytics: Boolean = false,",
        "  val canManageAdmins: Boolean = false",
        ")",
        "",
        "// Dispute.kt",
        "data class Dispute(",
        "  val id: String = \"\",",
        "  val bookingId: String = \"\",",
        "  val raisedBy: String = \"\",  // user | koshai",
        "  val raisedByUid: String = \"\",",
        "  val reason: String = \"\",",
        "  val description: String = \"\",",
        "  val status: String = \"open\",",
        "  val assignedAdmin: String = \"\",",
        "  val resolution: String = \"\",",
        "  val refundAmount: Int = 0,",
        "  val createdAt: com.google.firebase.Timestamp? = null",
        ")",
      ]),
      ...spacer(1),

      h2("7.3 Admin Auth & Role Check"),
      ...codeBlock([
        "// AdminAuthViewModel.kt",
        "@HiltViewModel",
        "class AdminAuthViewModel @Inject constructor(",
        "  private val auth: FirebaseAuth,",
        "  private val db: FirebaseFirestore",
        ") : ViewModel() {",
        "",
        "  sealed class AdminAuthState {",
        "    object Idle : AdminAuthState()",
        "    object Loading : AdminAuthState()",
        "    data class Success(val admin: Admin) : AdminAuthState()",
        "    data class Error(val message: String) : AdminAuthState()",
        "    object NotAdmin : AdminAuthState()",
        "    object Inactive : AdminAuthState()",
        "  }",
        "",
        "  private val _state = MutableStateFlow<AdminAuthState>(AdminAuthState.Idle)",
        "  val state: StateFlow<AdminAuthState> = _state",
        "",
        "  fun loginAdmin(email: String, password: String) {",
        "    _state.value = AdminAuthState.Loading",
        "    viewModelScope.launch {",
        "      try {",
        "        val result = auth.signInWithEmailAndPassword(email, password).await()",
        "        val uid = result.user?.uid ?: throw Exception(\"No UID\")",
        "        val adminDoc = db.collection(\"admins\").document(uid).get().await()",
        "        if (!adminDoc.exists()) {",
        "          auth.signOut()",
        "          _state.value = AdminAuthState.NotAdmin",
        "          return@launch",
        "        }",
        "        val admin = adminDoc.toObject(Admin::class.java)!!",
        "        if (!admin.isActive) {",
        "          auth.signOut()",
        "          _state.value = AdminAuthState.Inactive",
        "          return@launch",
        "        }",
        "        // Record last login",
        "        db.collection(\"admins\").document(uid)",
        "          .update(\"lastLoginAt\", com.google.firebase.Timestamp.now())",
        "        _state.value = AdminAuthState.Success(admin)",
        "      } catch (e: Exception) {",
        "        _state.value = AdminAuthState.Error(e.message ?: \"Login failed\")",
        "      }",
        "    }",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.4 Admin Koshai Verification ViewModel"),
      ...codeBlock([
        "@HiltViewModel",
        "class VerificationViewModel @Inject constructor(",
        "  private val db: FirebaseFirestore,",
        "  private val auth: FirebaseAuth",
        ") : ViewModel() {",
        "",
        "  val pendingKoshais = db.collection(\"koshais\")",
        "    .whereEqualTo(\"isVerified\", false)",
        "    .whereEqualTo(\"isBanned\", false)",
        "    .snapshots()",
        "    .map { it.toObjects(Koshai::class.java) }",
        "    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())",
        "",
        "  fun approveKoshai(koshaiId: String) {",
        "    val adminUid = auth.currentUser?.uid ?: return",
        "    viewModelScope.launch {",
        "      db.collection(\"koshais\").document(koshaiId).update(",
        "        mapOf(",
        "          \"isVerified\" to true,",
        "          \"approvedAt\" to com.google.firebase.Timestamp.now(),",
        "          \"approvedBy\" to adminUid",
        "        )",
        "      ).await()",
        "      // Notify koshai via FCM (handled by Cloud Function trigger)",
        "    }",
        "  }",
        "",
        "  fun rejectKoshai(koshaiId: String, reason: String) {",
        "    viewModelScope.launch {",
        "      db.collection(\"koshais\").document(koshaiId).update(",
        "        mapOf(\"status\" to \"offline\", \"rejectionReason\" to reason)",
        "      ).await()",
        "      db.collection(\"adminAlerts\").add(",
        "        mapOf(",
        "          \"type\" to \"koshai_rejected\",",
        "          \"koshaiId\" to koshaiId,",
        "          \"message\" to \"NID rejected: $reason\",",
        "          \"status\" to \"resolved\",",
        "          \"createdAt\" to com.google.firebase.FieldValue.serverTimestamp()",
        "        )",
        "      ).await()",
        "    }",
        "  }",
        "",
        "  fun banKoshai(koshaiId: String, reason: String) {",
        "    val adminUid = auth.currentUser?.uid ?: return",
        "    viewModelScope.launch {",
        "      db.collection(\"koshais\").document(koshaiId).update(",
        "        mapOf(",
        "          \"isBanned\" to true,",
        "          \"status\" to \"offline\",",
        "          \"banReason\" to reason,",
        "          \"bannedBy\" to adminUid",
        "        )",
        "      ).await()",
        "    }",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.5 Platform Settings ViewModel (Admin)"),
      ...codeBlock([
        "@HiltViewModel",
        "class PlatformSettingsViewModel @Inject constructor(",
        "  private val db: FirebaseFirestore,",
        "  private val auth: FirebaseAuth",
        ") : ViewModel() {",
        "",
        "  val settings = db.collection(\"platformSettings\").document(\"global\")",
        "    .snapshots()",
        "    .map { it.data }",
        "    .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())",
        "",
        "  fun toggleEidMode(active: Boolean, start: Date? = null, end: Date? = null) {",
        "    val adminUid = auth.currentUser?.uid ?: return",
        "    viewModelScope.launch {",
        "      val updates = mutableMapOf<String, Any>(",
        "        \"eidModeActive\" to active,",
        "        \"updatedBy\" to adminUid,",
        "        \"updatedAt\" to com.google.firebase.Timestamp.now()",
        "      )",
        "      start?.let { updates[\"eidStartDate\"] = com.google.firebase.Timestamp(it) }",
        "      end?.let { updates[\"eidEndDate\"] = com.google.firebase.Timestamp(it) }",
        "      db.collection(\"platformSettings\").document(\"global\").update(updates).await()",
        "    }",
        "  }",
        "",
        "  fun setMaintenanceMode(active: Boolean, message: String = \"\") {",
        "    val adminUid = auth.currentUser?.uid ?: return",
        "    viewModelScope.launch {",
        "      db.collection(\"platformSettings\").document(\"global\").update(",
        "        mapOf(",
        "          \"maintenanceMode\" to active,",
        "          \"maintenanceMessage\" to message,",
        "          \"updatedBy\" to adminUid",
        "        )",
        "      ).await()",
        "    }",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.6 HomeViewModel — Fetch & Filter Koshais"),
      ...codeBlock([
        "@HiltViewModel",
        "class HomeViewModel @Inject constructor(",
        "  private val repo: KoshaiRepository",
        ") : ViewModel() {",
        "  private val _koshais = MutableStateFlow<List<Koshai>>(emptyList())",
        "  val koshais: StateFlow<List<Koshai>> = _koshais",
        "  fun loadKoshais(district: String, animalType: String? = null) {",
        "    viewModelScope.launch {",
        "      repo.getAvailableKoshais(district, animalType).collect { _koshais.value = it }",
        "    }",
        "  }",
        "}",
        "",
        "class KoshaiRepository @Inject constructor(private val db: FirebaseFirestore) {",
        "  fun getAvailableKoshais(district: String, animal: String?) = callbackFlow {",
        "    var query: Query = db.collection(\"koshais\")",
        "      .whereEqualTo(\"district\", district)",
        "      .whereEqualTo(\"status\", \"available\")",
        "      .whereEqualTo(\"isVerified\", true)",
        "      .whereEqualTo(\"isBanned\", false)   // Never show banned koshais",
        "    if (animal == \"cow\") query = query.whereGreaterThan(\"ratePerCow\", 0)",
        "    if (animal == \"goat\") query = query.whereGreaterThan(\"ratePerGoat\", 0)",
        "    val listener = query.addSnapshotListener { snap, _ ->",
        "      trySend(snap?.toObjects(Koshai::class.java) ?: emptyList())",
        "    }",
        "    awaitClose { listener.remove() }",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.7 Booking Flow with Cost Estimator"),
      ...codeBlock([
        "class BookingViewModel @Inject constructor(",
        "  private val db: FirebaseFirestore, private val auth: FirebaseAuth",
        ") : ViewModel() {",
        "  fun estimateCost(koshai: Koshai, cows: Int, goats: Int): Int {",
        "    val base = (cows * koshai.ratePerCow) + (goats * koshai.ratePerGoat)",
        "    return (base * koshai.surgeMultiplier).toInt()",
        "  }",
        "  fun estimateDuration(cows: Int, goats: Int): Int = (cows * 45) + (goats * 25)",
        "  fun createBooking(booking: Booking, onSuccess: (String) -> Unit) {",
        "    viewModelScope.launch {",
        "      try {",
        "        val ref = db.collection(\"bookings\").document()",
        "        ref.set(booking.copy(id = ref.id)).await()",
        "        db.collection(\"koshais\").document(booking.koshaiId).update(\"status\", \"busy\").await()",
        "        onSuccess(ref.id)",
        "      } catch (e: Exception) { /* handle */ }",
        "    }",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.8 Real-Time Chat"),
      ...codeBlock([
        "class ChatViewModel @Inject constructor(",
        "  private val rtdb: FirebaseDatabase, private val auth: FirebaseAuth",
        ") : ViewModel() {",
        "  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())",
        "  val messages: StateFlow<List<ChatMessage>> = _messages",
        "  fun observeChat(bookingId: String) {",
        "    rtdb.getReference(\"chats/\$bookingId/messages\").orderByChild(\"timestamp\")",
        "      .addValueEventListener(object : ValueEventListener {",
        "        override fun onDataChange(snap: DataSnapshot) {",
        "          _messages.value = snap.children.mapNotNull { it.getValue(ChatMessage::class.java) }",
        "        }",
        "        override fun onCancelled(e: DatabaseError) {}",
        "      })",
        "  }",
        "  fun sendMessage(bookingId: String, text: String) {",
        "    val msg = ChatMessage(senderId = auth.currentUser?.uid ?: return, text = text,",
        "                          timestamp = System.currentTimeMillis())",
        "    rtdb.getReference(\"chats/\$bookingId/messages\").push().setValue(msg)",
        "  }",
        "}",
      ]),
      ...spacer(1),

      h2("7.9 Live Koshai Location Tracking"),
      ...codeBlock([
        "class KoshaiLocationService : Service() {",
        "  private lateinit var fusedClient: FusedLocationProviderClient",
        "  private val rtdb = Firebase.database",
        "  private val koshaiId = Firebase.auth.currentUser?.uid ?: \"\"",
        "  private val locationCallback = object : LocationCallback() {",
        "    override fun onLocationResult(result: LocationResult) {",
        "      result.lastLocation?.let { loc ->",
        "        rtdb.getReference(\"locations/\$koshaiId\").setValue(",
        "          mapOf(\"lat\" to loc.latitude, \"lng\" to loc.longitude,",
        "                \"updatedAt\" to System.currentTimeMillis()))",
        "      }",
        "    }",
        "  }",
        "  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {",
        "    val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000).build()",
        "    fusedClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper())",
        "    return START_STICKY",
        "  }",
        "  override fun onBind(intent: Intent?) = null",
        "}",
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 8. CLOUD FUNCTIONS ───────────────────────────────────────────────
      h1("8. Firebase Cloud Functions (Node.js)"),

      h2("8.1 Booking Reminder Function"),
      ...codeBlock([
        "export const sendBookingReminders = functions.pubsub.schedule('every 30 minutes').onRun(async () => {",
        "  const in24h = new Date(Date.now() + 24 * 3600 * 1000);",
        "  const snap24 = await db.collection('bookings')",
        "    .where('status', 'in', ['confirmed']).where('reminder24Sent', '==', false)",
        "    .where('date', '<=', admin.firestore.Timestamp.fromDate(in24h)).get();",
        "  for (const doc of snap24.docs) {",
        "    const b = doc.data();",
        "    await sendFCM(b.userFcmToken, '24-hour reminder', `Your koshai arrives tomorrow.`);",
        "    await sendFCM(b.koshaiToken, '24-hour reminder', `Booking tomorrow at ${b.slot}.`);",
        "    await doc.ref.update({ reminder24Sent: true });",
        "  }",
        "});",
      ]),
      ...spacer(1),

      h2("8.2 Fraud Detection & Admin Alert"),
      ...codeBlock([
        "export const detectFraud = functions.firestore",
        "  .document('bookings/{bookingId}').onCreate(async (snap) => {",
        "    const b = snap.data();",
        "    const todayStart = new Date(); todayStart.setHours(0,0,0,0);",
        "    const todaySnap = await db.collection('bookings')",
        "      .where('koshaiId', '==', b.koshaiId)",
        "      .where('status', 'in', ['pending','confirmed'])",
        "      .where('date', '>=', admin.firestore.Timestamp.fromDate(todayStart)).get();",
        "    if (todaySnap.size > 25) {",
        "      await db.collection('koshais').doc(b.koshaiId)",
        "        .update({ status: 'offline', isFlagged: true });",
        "      await db.collection('adminAlerts').add({",
        "        type: 'fraud_suspected', koshaiId: b.koshaiId, severity: 'high',",
        "        message: `Koshai has ${todaySnap.size} bookings today — possible fraud.`,",
        "        status: 'open', createdAt: admin.firestore.FieldValue.serverTimestamp()",
        "      });",
        "    }",
        "  });",
      ]),
      ...spacer(1),

      h2("8.3 Koshai Approval Notification"),
      ...codeBlock([
        "// Fires when admin sets isVerified = true on a koshai document",
        "export const onKoshaiVerified = functions.firestore",
        "  .document('koshais/{koshaiId}').onUpdate(async (change) => {",
        "    const before = change.before.data();",
        "    const after  = change.after.data();",
        "    if (!before.isVerified && after.isVerified) {",
        "      await sendFCM(after.fcmToken, 'You are now Verified!',",
        "        'Your Koshai Finder profile has been approved. You can now accept bookings.');",
        "    }",
        "    if (!before.isBanned && after.isBanned) {",
        "      await sendFCM(after.fcmToken, 'Account Suspended',",
        "        `Your account has been suspended. Reason: ${after.banReason}`);",
        "    }",
        "  });",
      ]),
      ...spacer(1),

      h2("8.4 Rating Average Update"),
      ...codeBlock([
        "export const updateKoshaiRating = functions.firestore",
        "  .document('reviews/{reviewId}').onCreate(async (snap) => {",
        "    const review = snap.data();",
        "    const koshaiRef = db.collection('koshais').doc(review.koshaiId);",
        "    await db.runTransaction(async (tx) => {",
        "      const kDoc = await tx.get(koshaiRef);",
        "      const current = kDoc.data()?.rating ?? 0;",
        "      const count   = kDoc.data()?.totalRatings ?? 0;",
        "      const newAvg  = ((current * count) + review.rating) / (count + 1);",
        "      tx.update(koshaiRef, { rating: Math.round(newAvg * 10) / 10, totalRatings: count + 1 });",
        "    });",
        "  });",
      ]),
      ...spacer(1),

      h2("8.5 Dispute Auto-Alert"),
      ...codeBlock([
        "// When a dispute is created, auto-create an admin alert",
        "export const onDisputeCreated = functions.firestore",
        "  .document('disputes/{disputeId}').onCreate(async (snap) => {",
        "    const d = snap.data();",
        "    await db.collection('adminAlerts').add({",
        "      type: 'dispute',",
        "      bookingId: d.bookingId,",
        "      message: `New dispute from ${d.raisedBy}: ${d.reason}`,",
        "      severity: 'medium',",
        "      status: 'open',",
        "      createdAt: admin.firestore.FieldValue.serverTimestamp()",
        "    });",
        "  });",
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 9. ALL 22 FEATURES ───────────────────────────────────────────────
      h1("9. All 22 Features — Detailed Specification"),

      h2("Phase 1 — MVP (Weeks 1–6)"),
      simpleTable(
        ["#", "Feature", "Role", "Implementation", "Firebase Service"],
        [
          ["1", "Phone OTP auth", "User / Koshai", "PhoneAuthProvider with +88 prefix, role selection post-auth", "Firebase Auth"],
          ["2", "Admin email/password auth", "Admin", "Email+password login, admins/{uid} check, permission load", "Firebase Auth, Firestore"],
          ["3", "Koshai registration", "Koshai", "NID photo upload to Storage, Firestore write, pending state", "Auth, Storage, Firestore"],
          ["4", "Home map view", "User", "Google Maps with real-time Firestore listener for verified, unbanned pins", "Firestore, Maps SDK"],
          ["5", "Slot-based booking", "User / Koshai", "Time slots per koshai day, conflict check before write", "Firestore"],
          ["6", "Push notifications", "All", "FCM tokens stored per user/koshai/admin, Cloud Functions trigger", "FCM, Cloud Functions"],
        ],
        [300, 2000, 1200, 3260, 2600]
      ),
      ...spacer(1),

      h2("Phase 2 — Trust (Weeks 7–12)"),
      simpleTable(
        ["#", "Feature", "Role", "Implementation", "Firebase Service"],
        [
          ["7", "Koshai verification queue", "Admin", "Admin mobile panel + web dashboard show koshais with isVerified=false; approve sets flag + notifies via Cloud Function", "Firestore, FCM, Functions"],
          ["8", "Ban / unban koshai", "Admin", "Admin sets isBanned=true; Cloud Function notifies koshai; banned koshais hidden from all searches", "Firestore, FCM, Functions"],
          ["9", "Live booking tracker", "User / Koshai", "Realtime DB status updates + koshai GPS on mini-map", "Realtime DB, Maps SDK"],
          ["10", "In-app chat", "User / Koshai", "Realtime DB chat per bookingId, no phone number sharing", "Realtime Database"],
          ["11", "Ratings & photo reviews", "User", "Post-completion form, Cloud Function updates rating average; admin can hide reviews", "Firestore, Storage, Functions"],
          ["12", "Animal count estimator", "User", "Local Kotlin calculation: count x rate x surgeMultiplier", "None (local logic)"],
        ],
        [300, 2000, 1200, 3260, 2600]
      ),
      ...spacer(1),

      h2("Phase 3 — Growth (Weeks 13–18)"),
      simpleTable(
        ["#", "Feature", "Role", "Implementation", "Firebase Service"],
        [
          ["13", "bKash / Nagad payment", "User", "Retrofit call to bKash PGW API, store txn ref in Firestore", "Firestore, Functions"],
          ["14", "Dispute system", "User / Koshai / Admin", "User/koshai raises dispute; Cloud Function creates admin alert; admin resolves with refund note", "Firestore, FCM, Functions"],
          ["15", "Platform settings panel", "Admin", "Admin controls Eid mode dates, surge cap, commission %, maintenance mode from web dashboard", "Firestore"],
          ["16", "Surge pricing (Eid mode)", "Admin / Koshai", "Admin enables globally via platformSettings; koshais can also toggle their own surgeMultiplier within cap", "Firestore, Functions"],
          ["17", "Waitlist system", "User", "Ordered subcollection per koshai, trigger on booking cancel", "Firestore, FCM"],
          ["18", "Smart reminders", "User / Koshai", "Scheduled Cloud Function checks bookings every 30 mins", "Cloud Functions, FCM"],
        ],
        [300, 2000, 1200, 3260, 2600]
      ),
      ...spacer(1),

      h2("Phase 4 — Scale (Weeks 19–24)"),
      simpleTable(
        ["#", "Feature", "Role", "Implementation", "Firebase Service"],
        [
          ["19", "Group / neighborhood booking", "User", "groupSessions collection, join code, shared koshai slot, travel fee split", "Firestore"],
          ["20", "Demand heatmap", "User / Admin", "Aggregate Firestore data by upazila, Maps JS API heatmap layer visible to users and in admin dashboard", "Firestore, Maps SDK"],
          ["21", "Koshai leaderboard", "Koshai / Admin", "Nightly Cloud Function ranks koshais by rating+jobs per district; visible in admin analytics", "Firestore, Functions"],
          ["22", "Admin analytics dashboard", "Admin", "React web app: daily KPIs, revenue trends, booking volume, top koshais, district coverage charts", "Firestore, Auth, Functions"],
        ],
        [300, 2000, 1200, 3260, 2600]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 10. 24-WEEK PIPELINE ─────────────────────────────────────────────
      h1("10. 24-Week Development Pipeline"),

      h2("Phase 1 — Foundation & MVP (Weeks 1–6)"),
      phaseTable([
        ["Week", "Task", "Deliverable", "Owner"],
        ["1", "Firebase project setup, google-services.json, MVVM + Hilt + Navigation scaffold. Create users, koshais, admins, platformSettings collections. Set up admin email auth separately.", "Buildable empty app with Firebase + 3-role architecture wired", "Dev"],
        ["2", "Auth flow: phone OTP for user/koshai role select; email+password for admin entry point; Firestore role check on login; AdminLoginFragment + permission load.", "Working 3-way login routing", "Dev"],
        ["3", "Koshai registration: NID + photo upload, coverage area, rates, pending state. Admin mobile panel: VerificationQueueFragment with approve/reject actions.", "Koshais register; admin can approve from phone", "Dev"],
        ["4", "Google Maps integration: fetch verified, non-banned koshais, plot color pins. Filter by district. Tap to profile.", "Live map showing only verified koshais", "Dev"],
        ["5", "Slot booking flow: profile screen, slot picker, booking form, Firestore write, koshai dashboard accept/decline, admin can view all bookings.", "End-to-end booking visible to all 3 roles", "Dev"],
        ["6", "FCM push notifications on booking events; availability toggle on koshai side; AdminDashboardFragment with alert counter; internal TestFlight APK.", "Phase 1 APK for internal testing", "Dev + QA"],
      ]),
      ...spacer(1),

      h2("Phase 2 — Trust Layer (Weeks 7–12)"),
      phaseTable([
        ["Week", "Task", "Deliverable", "Owner"],
        ["7", "Full koshai verification workflow: admin approves/rejects in mobile panel + web stub; Cloud Function notifies koshai on approval; verified badge shown in UI.", "Verified badge workflow end-to-end", "Dev"],
        ["8", "Ban/unban feature: admin sets isBanned, Cloud Function sends FCM to koshai, all Firestore queries filter out banned koshais.", "Ban system working; banned koshais invisible to users", "Dev"],
        ["9", "Live booking tracker: Realtime DB status pipeline (confirmed->en_route->arrived->done), mini-map on tracker screen.", "Real-time tracking works for users", "Dev"],
        ["10", "In-app chat per booking; ReviewFragment with star rating, text, photo; admin review hide button; Cloud Function updates rating average.", "Chat + reviews working; admin can moderate reviews", "Dev"],
        ["11", "Animal count estimator + auto cost/duration; koshai profile surge indicator; dispute form for user and koshai; DisputeManagerFragment for admin.", "Dispute submission and admin resolution flow complete", "Dev"],
        ["12", "Phase 2 full QA, edge case fixes, beta APK release.", "Phase 2 beta APK", "Dev + QA"],
      ]),
      ...spacer(1),

      h2("Phase 3 — Growth Features (Weeks 13–18)"),
      phaseTable([
        ["Week", "Task", "Deliverable", "Owner"],
        ["13", "bKash sandbox integration: PaymentFragment, Retrofit call, store txn ref in booking. Admin can view payment status on booking detail.", "Deposit payment works in sandbox", "Dev"],
        ["14", "Nagad integration, production bKash credentials, payment failure/retry, refund flow triggered by admin dispute resolution.", "Both payment gateways in production; refunds admin-triggered", "Dev"],
        ["15", "PlatformSettings web page: Eid mode toggle with date range, surge cap slider, commission %, maintenance mode banner. Cloud Function reads settings on booking creation.", "Admin controls Eid mode; surge applies globally", "Dev"],
        ["16", "Koshai-level surge: koshais set own surgeMultiplier within platform cap; surgeMultiplier shown to users on booking form as transparent breakdown.", "Per-koshai surge pricing with platform ceiling", "Dev"],
        ["17", "Waitlist system + Smart 24h/2h reminder Cloud Functions. Admin AlertCenterFragment shows open waitlist alerts.", "Waitlist and reminders work end-to-end", "Dev"],
        ["18", "Group booking: session creation, join code, travel fee split. Phase 3 full QA, performance testing, Play Store submission.", "App live on Google Play Store", "Dev + QA"],
      ]),
      ...spacer(1),

      h2("Phase 4 — Scale & Admin Analytics (Weeks 19–24)"),
      phaseTable([
        ["Week", "Task", "Deliverable", "Owner"],
        ["19", "Demand heatmap: aggregate bookings by upazila via Cloud Function, display HeatmapTileProvider on home map and in admin web dashboard.", "Heatmap live for users and admin", "Dev"],
        ["20", "Leaderboard: nightly Cloud Function ranks by rating+jobs per district. Visible to koshais (own rank) and to admin (full table).", "Leaderboard updates nightly", "Dev"],
        ["21", "Admin analytics dashboard (React): KPI cards, revenue chart, booking volume trend, top koshais table, district coverage. Hosted on Firebase Hosting.", "Admin dashboard live at admin.koshaiapp.com", "Dev"],
        ["22", "Admin management: superadmin can create/deactivate admin accounts, assign roles and permissions from web dashboard.", "Role-based admin management working", "Dev"],
        ["23", "Fraud detection Cloud Function, admin alert auto-creation, account flagging. CI/CD GitHub Actions: automated AAB build + Play Store upload.", "Fraud alerts in admin dashboard; CI/CD pipeline live", "Dev"],
        ["24", "Full regression QA across all 3 roles, app store optimization, admin onboarding guide, v2.0 release.", "v2.0 shipped with full 3-role feature set", "Dev + QA"],
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 11. FOLDER STRUCTURE ─────────────────────────────────────────────
      h1("11. Project Folder Structure"),
      ...codeBlock([
        "app/src/main/java/com/koshaiapp/",
        "  di/                         // Hilt modules (FirebaseModule, etc.)",
        "  data/",
        "    model/                    // Koshai.kt, Booking.kt, Review.kt, Admin.kt, Dispute.kt",
        "    repository/               // KoshaiRepository, BookingRepository, AdminRepository",
        "    remote/                   // BkashApiService.kt (Retrofit)",
        "  domain/",
        "    usecase/                  // GetKoshaisUseCase, CreateBookingUseCase, VerifyKoshaiUseCase",
        "  ui/",
        "    auth/                     // PhoneInputFragment, OtpVerifyFragment, RoleSelectFragment",
        "    admin/                    // AdminLoginFragment, AdminDashboardFragment",
        "                              // VerificationQueueFragment, AlertCenterFragment",
        "                              // DisputeManagerFragment, PlatformSettingsFragment",
        "    home/                     // HomeFragment, HomeViewModel, SearchFilterFragment",
        "    koshai/                   // KoshaiProfileFragment, KoshaiProfileViewModel",
        "    booking/                  // BookingFormFragment, BookingViewModel, PaymentFragment",
        "    tracker/                  // BookingTrackerFragment",
        "    chat/                     // ChatFragment, ChatViewModel",
        "    mybookings/               // MyBookingsFragment",
        "    review/                   // WriteReviewFragment",
        "    koshaidashboard/          // KoshaiDashboardFragment, AvailabilityFragment",
        "    bookingmgr/               // BookingManagerFragment, BookingDetailFragment",
        "    earnings/                 // EarningsFragment",
        "    leaderboard/              // LeaderboardFragment",
        "  service/",
        "    KoshaiLocationService.kt",
        "    FirebaseMessagingService.kt",
        "  util/                       // Extensions, Constants, DateUtils, AdminRoleGuard",
        "  res/",
        "    layout/                   // All XML layouts (admin screens included)",
        "    navigation/               // nav_graph_user.xml, nav_graph_admin.xml",
        "    values/                   // strings.xml (English)",
        "    values-b+bn/              // strings.xml (Bangla)",
        "",
        "functions/                    // Firebase Cloud Functions (TypeScript)",
        "  src/",
        "    reminders.ts",
        "    fraud.ts",
        "    ratings.ts",
        "    waitlist.ts",
        "    eidMode.ts",
        "    disputes.ts               // NEW: dispute alert triggers",
        "    koshaiVerification.ts     // NEW: approval/ban FCM triggers",
        "",
        "admin-dashboard/              // React web app (Firebase Hosting)",
        "  src/",
        "    pages/                    // Dashboard, Koshais, Bookings, Disputes, Settings",
        "    components/               // KoshaiCard, AlertBadge, BookingTable, ChartCard",
        "    hooks/                    // useAdminAuth, useAdminAlerts, usePlatformSettings",
        "    firebase.ts               // Firebase init for web",
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 12. PLAY STORE & CI/CD ────────────────────────────────────────────
      h1("12. Play Store Submission & CI/CD"),
      h2("12.1 Google Play Store Checklist"),
      numbered("Create Google Play Console account (one-time $25 USD fee)"),
      numbered("App signing: generate release keystore with keytool, store in GitHub Secrets"),
      numbered("App bundle: build release AAB — smaller size, better Play optimization"),
      numbered("Store listing: Bangla + English description, screenshots from all key screens"),
      numbered("Content rating: complete IARC questionnaire (expected: Everyone)"),
      numbered("Privacy policy: host at koshaiapp.com/privacy — required for payment apps"),
      numbered("Data safety form: declare Firebase Analytics, phone number collection"),
      numbered("Target SDK: API 34 (required by Play Store from August 2024 onwards)"),
      ...spacer(1),
      h2("12.2 GitHub Actions CI/CD Pipeline"),
      ...codeBlock([
        "# .github/workflows/release.yml",
        "name: Build & Deploy Release APK",
        "on:",
        "  push:",
        "    branches: [ main ]",
        "    tags: [ 'v*' ]",
        "jobs:",
        "  build:",
        "    runs-on: ubuntu-latest",
        "    steps:",
        "      - uses: actions/checkout@v4",
        "      - uses: actions/setup-java@v4",
        "        with: { java-version: '17', distribution: 'temurin' }",
        "      - name: Decode google-services.json",
        "        run: echo ${{ secrets.GOOGLE_SERVICES_JSON }} | base64 -d > app/google-services.json",
        "      - name: Build Release AAB",
        "        run: ./gradlew bundleRelease",
        "        env:",
        "          KEYSTORE_FILE: ${{ secrets.KEYSTORE_B64 }}",
        "          KEY_ALIAS:     ${{ secrets.KEY_ALIAS }}",
        "          KEY_PASSWORD:  ${{ secrets.KEY_PASSWORD }}",
        "      - name: Upload to Play Store",
        "        uses: r0adkll/upload-google-play@v1",
        "        with:",
        "          serviceAccountJsonPlainText: ${{ secrets.PLAY_SA_JSON }}",
        "          packageName: com.koshaiapp",
        "          releaseFiles: app/build/outputs/bundle/release/*.aab",
        "          track: internal",
        "  deploy-admin:",
        "    runs-on: ubuntu-latest",
        "    steps:",
        "      - uses: actions/checkout@v4",
        "      - run: cd admin-dashboard && npm ci && npm run build",
        "      - uses: FirebaseExtended/action-hosting-deploy@v0",
        "        with:",
        "          repoToken: ${{ secrets.GITHUB_TOKEN }}",
        "          firebaseServiceAccount: ${{ secrets.FIREBASE_SA_JSON }}",
        "          channelId: live",
        "          projectId: koshai-finder",
      ]),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 13. MONETIZATION ─────────────────────────────────────────────────
      h1("13. Monetization Strategy"),
      simpleTable(
        ["Revenue Stream", "Model", "Estimated Revenue"],
        [
          ["Booking deposit", "User pays 5-10% deposit via bKash/Nagad at booking. Admin controls % via platformSettings.", "Core revenue from day 1"],
          ["Platform commission", "5% commission on each completed booking. Admin-adjustable via platformSettings.", "Scales with transaction volume"],
          ["Featured listings", "Koshais pay BDT 200–500 for top search placement during Eid. Admin enables this tier.", "High demand period upsell"],
          ["Premium koshai", "Verified Pro tier BDT 500/year — priority support, analytics dashboard.", "Recurring annual revenue"],
          ["Group booking fee", "BDT 50 organizer fee per group session. Auto-collected on session creation.", "Low friction, high volume"],
          ["Referral commissions", "2% of first 5 bookings made by referred koshais.", "Growth flywheel incentive"],
        ],
        [2800, 3600, 2960]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 14. ADMIN ROLE DETAIL ─────────────────────────────────────────────
      h1("14. Admin Role — Full Specification"),
      h2purple("14.1 Admin Permission Levels"),
      adminTable(
        ["Permission", "Superadmin", "Moderator", "Support"],
        [
          ["Verify / reject koshais", "Yes", "Yes", "No"],
          ["Ban / unban koshais or users", "Yes", "Yes", "No"],
          ["Resolve disputes + trigger refunds", "Yes", "Yes", "Yes (read-only)"],
          ["Edit platform settings (Eid mode, surge, commission)", "Yes", "No", "No"],
          ["View analytics dashboard", "Yes", "Yes", "Yes"],
          ["Create / deactivate admin accounts", "Yes", "No", "No"],
          ["Hide / restore reviews", "Yes", "Yes", "No"],
          ["Add admin notes to bookings", "Yes", "Yes", "Yes"],
          ["Cancel any booking", "Yes", "Yes", "No"],
        ],
        [3600, 1920, 1920, 1920]
      ),
      ...spacer(1),
      h2purple("14.2 Admin Onboarding Process"),
      numbered("Superadmin creates new admin account via admin web dashboard (/settings -> Manage Admins)"),
      numbered("System calls Firebase Auth Admin SDK to create email/password user; sets isActive=true in admins collection"),
      numbered("New admin receives email invite with temporary password"),
      numbered("Admin logs in via AdminLoginFragment on app or admin.koshaiapp.com; forced password reset on first login"),
      numbered("Permissions are read from admins/{uid}.permissions — no client-side permission logic; all enforced in Firestore rules and Cloud Functions"),
      ...spacer(1),
      h2purple("14.3 Admin Alert Severity Guide"),
      adminTable(
        ["Severity", "Color", "Examples", "SLA"],
        [
          ["Critical", "Red", "Payment failure affecting many bookings, system-wide error, data breach suspicion", "Resolve within 1 hour"],
          ["High", "Orange", "Fraud flag on koshai with 25+ bookings, repeat no-show koshai, user complaint with evidence", "Resolve within 4 hours"],
          ["Medium", "Amber", "New dispute raised, abusive review reported, suspicious refund request", "Resolve within 24 hours"],
          ["Low", "Blue", "New NID pending verification, standard koshai registration, minor app feedback", "Resolve within 72 hours"],
        ],
        [1400, 1200, 4360, 2400]
      ),
      ...spacer(1),
      h2purple("14.4 Admin Web Dashboard Pages (React)"),
      para("Hosted at admin.koshaiapp.com on Firebase Hosting. Uses Firebase SDK (web) with admin email auth. All data reads go directly to Firestore — no separate API needed."),
      ...spacer(1),
      adminTable(
        ["Page", "Key Components", "Data Source"],
        [
          ["/dashboard", "KPI cards (daily bookings, revenue, new users, open alerts), booking trend line chart, district coverage pie chart, live admin online indicator", "Firestore aggregation queries + Realtime DB adminOnline"],
          ["/koshais", "Searchable/filterable table of all koshais. Actions: Approve NID, Reject with reason, Ban with reason, Unban, Set surge cap. Inline NID image viewer.", "koshais collection"],
          ["/bookings", "All bookings with filters by status/district/date. Admin can add note, cancel booking, view payment ref. Export to CSV.", "bookings collection"],
          ["/disputes", "Unified disputes + adminAlerts feed. Assign to self, change status, write resolution, input refund amount. Filter by type and severity.", "disputes + adminAlerts collections"],
          ["/settings", "Eid mode toggle with date pickers, surge cap slider, booking deposit %, platform commission %, maintenance mode on/off with custom message, admin account management table.", "platformSettings/global + admins collection"],
        ],
        [1600, 4560, 3200]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 15. RISKS ────────────────────────────────────────────────────────
      h1("15. Risks & Mitigations"),
      simpleTable(
        ["Risk", "Likelihood", "Mitigation"],
        [
          ["Koshais don't own smartphones", "Medium", "Partner with local mosque committees and butcher associations for group onboarding; SMS fallback for confirmations"],
          ["bKash API approval delays", "Medium", "Start with cash-on-delivery; bKash added in Phase 3 only after approval"],
          ["Low supply at launch", "High", "Pre-register koshais before launch via Facebook/WhatsApp outreach in major districts"],
          ["No-shows despite deposit", "Low", "Deposit non-refundable if cancelled within 12h; repeat no-shows flagged to admin for review"],
          ["Admin account compromise", "Low", "Email+password auth with 2FA recommended; superadmin can instantly deactivate any admin via isActive=false; all admin actions logged"],
          ["Firebase costs scaling", "Low", "Free tier handles ~50k reads/day; Firestore index budgets and listener cleanup to control costs"],
          ["Google Play rejection", "Low", "Follow all data safety requirements, privacy policy; avoid sensitive permissions without justification"],
          ["Competitor copies the idea", "Medium", "Move fast, build community trust, lock in verified koshai supply side before competitors"],
        ],
        [2800, 1400, 5160]
      ),
      new Paragraph({ children: [new PageBreak()] }),

      // ── 16. APPENDIX ─────────────────────────────────────────────────────
      h1("16. Appendix — Quick Reference"),
      h2("16.1 Districts to Support at Launch"),
      bullet("Dhaka (highest density, largest Eid demand)"),
      bullet("Narayanganj (app founder's base)"),
      bullet("Chattogram (second largest city)"),
      bullet("Sylhet (diaspora remittance, high purchasing power)"),
      bullet("Rajshahi (cattle-producing region, strong koshai supply)"),
      bullet("Khulna, Cumilla, Gazipur (Phase 2 expansion)"),
      ...spacer(1),
      h2("16.2 Key API Credentials Needed"),
      bullet("Firebase: google-services.json from Firebase Console"),
      bullet("Google Maps: Maps SDK API key with Maps, Places, Directions enabled"),
      bullet("bKash PGW: Merchant account from bKash merchant portal"),
      bullet("Nagad: Merchant account from Nagad merchant portal"),
      bullet("Firebase Admin SDK: service account JSON for Cloud Functions + admin web app"),
      ...spacer(1),
      h2("16.3 Useful Links"),
      bullet("Firebase Console: console.firebase.google.com"),
      bullet("Google Play Console: play.google.com/console"),
      bullet("Google Maps Platform: console.cloud.google.com/google/maps-apis"),
      bullet("bKash Merchant: merchant.bkash.com"),
      bullet("Admin Dashboard (deploy target): admin.koshaiapp.com"),
      ...spacer(1),
      colorBox("This document covers the complete Koshai Finder app — 3 roles (User, Koshai, Admin), 22 features, 4 phases, 24 weeks, full Kotlin + Firebase + React implementation. Build Phase 1 first, get real users, then expand.", LIGHT_GREEN, "1D6B45"),
      ...spacer(1),
      purpleBox("Admin role includes: email/password auth, permission tiers (superadmin/moderator/support), mobile panel (5 screens), web dashboard (5 pages), Firestore security rules, Cloud Function triggers, dispute resolution, fraud alerts, platform settings control, and analytics."),
    ]
  }]
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('/mnt/user-data/outputs/koshai_finder_with_admin_role.docx', buffer);
  console.log('Done!');
});